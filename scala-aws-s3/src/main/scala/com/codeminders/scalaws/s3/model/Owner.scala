package com.codeminders.scalaws.s3.model

class Owner(val uid: String, val displayName: String){
  override def equals(any: Any): Boolean = {
    if (any.isInstanceOf[Owner]) {
      val that: Owner = any.asInstanceOf[Owner]
      that.uid.equals(this.uid) && that.displayName.equals(this.displayName)
    } else {
      false
    }
  }

  override def hashCode(): Int = {
    41 * (
      41 + this.uid.hashCode()) + this.displayName.hashCode()
  }

  override def toString: String = {
    "User[id=%s,displayName=%s]".format(uid, displayName)
  }
}

