package com.codeminders.scalaws.s3.model
import scala.xml.Elem
import com.codeminders.scalaws.http.Request
import scala.io.Source
import scala.collection._

object Permission extends Enumeration {
  type Permission = Value
  val READ = Value("x-amz-grant-read")
  val WRITE = Value("x-amz-grant-write")
  val READ_ACP = Value("x-amz-grant-read-acp")
  val WRITE_ACP = Value("x-amz-grant-write-acp")
  val FULL_CONTROL = Value("x-amz-grant-full-control")
}

import Permission._

class ACL(val owner: Owner, val grants: List[Grant]) {
  def toXML {
    <AccessControlPolicy xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
      <Owner>
        <ID>{ owner.id }</ID>
        <DisplayName>{ owner.displayName }</DisplayName>
      </Owner>
      <AccessControlList>
        {
          for (g <- grants) yield <Grant>
                                    {
                                      g.grantee match {
                                        case canonical: CanonicalGrantee =>
                                          <Grantee xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="CanonicalUser">
                                            <ID>{ canonical.id }</ID>
                                            <DisplayName>{ canonical.displayName }</DisplayName>
                                          </Grantee>
                                        case email: EmailAddressGrantee =>
                                          <Grantee xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="AmazonCustomerByEmail">
                                            <EmailAddress>{ email.emailAddress }</EmailAddress>
                                          </Grantee>
                                        case group: GroupGrantee =>
                                          <Grantee xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="Group">
                                            <URI>{ group.url }</URI>
                                          </Grantee>
                                      }

                                    }
                                  </Grant>
        }
      </AccessControlList>
    </AccessControlPolicy>
  }
}

class Grant(val grantee: Grantee, val permission: Permission)

trait Grantee

class CanonicalGrantee(val id: String, val displayName: String) extends Grantee

class EmailAddressGrantee(val emailAddress: String) extends Grantee

class GroupGrantee(val url: String) extends Grantee

object GroupGrantee {
  val AllUsers = new GroupGrantee("http://acs.amazonaws.com/groups/global/AllUsers")
  val AuthenticatedUsers = new GroupGrantee("http://acs.amazonaws.com/groups/global/AuthenticatedUsers")
  val LogDelivery = new GroupGrantee("http://acs.amazonaws.com/groups/s3/LogDelivery")
}

class CannedACL(aclValue: String) {
  override def toString(): String = {
    aclValue
  }
}

object CannedACL {
  def Private = new CannedACL("private")
  def PublicRead = new CannedACL("public-read")
  def PublicReadWrite = new CannedACL("public-read-write")
  def AuthenticatedRead = new CannedACL("authenticated-read")
  def BucketOwnerRead = new CannedACL("bucket-owner-read")
  def BucketOwnerFullControl = new CannedACL("bucket-owner-full-control")
  def LogDeliveryWrite = new CannedACL("log-delivery-write")
}

class ExplicitACL extends Traversable[(Permission, List[String])] {
  val acl = mutable.Map[Permission, List[String]]()

  def addPermission(p: Permission, id: String) = {
    acl.get(p) match {
      case None => acl += (p -> List(id))
      case Some(s) => acl += (p -> (id :: s))
    }

  }

  def foreach[U](f: ((Permission, List[String])) => U) = {
    acl.foreach(e => f(e))
  }

}