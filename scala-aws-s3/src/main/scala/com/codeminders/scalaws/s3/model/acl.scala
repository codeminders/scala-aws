package com.codeminders.scalaws.s3.model
import scala.xml.Elem
import scala.io.Source
import scala.collection._
import scala.xml.XML
import scala.xml.Node

object Permission extends Enumeration {
  type Permission = Value
  val READ, WRITE, READ_ACP, WRITE_ACP, FULL_CONTROL = Value
}

import Permission._

class ACL(val owner: Owner, val grants: Seq[Grant]) {
  def toXML: Node = {
    scala.xml.Utility.trim(<AccessControlPolicy xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
                             <Owner>
                               <ID>{ owner.uid }</ID>
                               <DisplayName>{ owner.displayName }</DisplayName>
                             </Owner>
                             <AccessControlList>
                               {
                                 for (g <- grants) yield <Grant>
                                                           {
                                                             g.grantee match {
                                                               case canonical: CanonicalGrantee =>
                                                                 <Grantee xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="CanonicalUser">
                                                                   <ID>{ canonical.uid }</ID>
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
                                                           <Permission>{ g.permission }</Permission>
                                                         </Grant>
                               }
                             </AccessControlList>
                           </AccessControlPolicy>)
  }
}

object ACL {
  def apply(xml: Node): ACL = {
    def extractOwner(node: scala.xml.Node): Owner =
      node match {
        case <Owner><ID>{ ownerID }</ID><DisplayName>{ displayName }</DisplayName></Owner> =>
          new Owner(ownerID.text, displayName.text)
      }

    def extractGrant(node: scala.xml.Node): Grant =
      node match {
        case <Grant><Grantee><ID>{ granteeID }</ID><DisplayName>{ granteeDisplayName }</DisplayName></Grantee><Permission>{ permission }</Permission></Grant> =>
          new Grant(new CanonicalGrantee(granteeID.text, granteeDisplayName.text), Permission.withName(permission.text))
        case <Grant><Grantee><EmailAddress>{ emailAddress }</EmailAddress></Grantee><Permission>{ permission }</Permission></Grant> =>
          new Grant(new EmailAddressGrantee(emailAddress.text), Permission.withName(permission.text))
        case <Grant><Grantee><URI>{ url }</URI></Grantee><Permission>{ permission }</Permission></Grant> =>
          new Grant(new GroupGrantee(url.text), Permission.withName(permission.text))
      }

    new ACL(extractOwner((xml \ "Owner").head), (xml \\ "Grant").foldLeft(Array[Grant]())((a, g) => a :+ extractGrant(g)))
  }
}

class Grant(val grantee: Grantee, val permission: Permission) {
  override def equals(any: Any): Boolean = {
    if (any.isInstanceOf[Grant]) {
      val that: Grant = any.asInstanceOf[Grant]
      that.grantee.equals(this.grantee) && that.permission.equals(this.permission)
    } else {
      false
    }
  }

  override def hashCode(): Int = {
    41 * (
      41 + this.grantee.hashCode()) + this.permission.hashCode()
  }

  override def toString: String = {
    "Grant[grantee=%s,permission=%s]".format(grantee.toString(), permission.toString())
  }
}

trait Grantee {
  def granteeID: String
}

class CanonicalGrantee(val uid: String, val displayName: String) extends Grantee {
  override def equals(any: Any): Boolean = {
    if (any.isInstanceOf[CanonicalGrantee]) {
      val that: CanonicalGrantee = any.asInstanceOf[CanonicalGrantee]
      that.uid.equals(this.uid) && that.displayName.equals(this.displayName)
    } else {
      false
    }
  }
  override def hashCode(): Int = 41 * (
    41 + this.uid.hashCode()) + this.displayName.hashCode()

  override def granteeID = uid

  override def toString: String = {
    "CanonicalGrantee[id=%s,displayName=%s]".format(uid, displayName)
  }
}

class EmailAddressGrantee(val emailAddress: String) extends Grantee {
  override def equals(any: Any): Boolean = {
    if (any.isInstanceOf[GroupGrantee]) {
      val that: EmailAddressGrantee = any.asInstanceOf[EmailAddressGrantee]
      that.emailAddress.equals(this.emailAddress)
    } else {
      false
    }
  }
  override def hashCode(): Int = this.emailAddress.hashCode

  override def granteeID = """emailAddress="%s"""".format(emailAddress)

  override def toString: String = {
    "EmailAddressGrantee[%s]".format(emailAddress)
  }
}

class GroupGrantee(val url: String) extends Grantee {
  override def equals(any: Any): Boolean = {
    if (any.isInstanceOf[GroupGrantee]) {
      val that: GroupGrantee = any.asInstanceOf[GroupGrantee]
      that.url.equals(this.url)
    } else {
      false
    }
  }
  override def hashCode(): Int = this.url.hashCode

  override def granteeID = """uri="%s"""".format(url)

  override def toString: String = {
    "GroupGrantee[%s]".format(url)
  }
}

object GroupGrantee {
  val AllUsers = new GroupGrantee("http://acs.amazonaws.com/groups/global/AllUsers")
  val AuthenticatedUsers = new GroupGrantee("http://acs.amazonaws.com/groups/global/AuthenticatedUsers")
  val LogDelivery = new GroupGrantee("http://acs.amazonaws.com/groups/s3/LogDelivery")
}

object CannedACL extends Enumeration {
  type CannedACL = Value
  val Private = Value("private")
  val PublicRead = Value("public-read")
  val PublicReadWrite = Value("public-read-write")
  val AuthenticatedRead = Value("authenticated-read")
  val BucketOwnerRead = Value("bucket-owner-read")
  val BucketOwnerFullControl = Value("bucket-owner-full-control")
  val LogDeliveryWrite = Value("log-delivery-write")
}