package com.example.engsa

import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey

import android.os.Build
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.internal.interop.RealmListT

//class UserDataClassRealm: Object, ObjectKeyIdentifiable {
//    @Persisted(primaryKey: true) var _id: UUID
//    //    @Persisted var ownerId = UIDevice.current.identifierForVendor!.uuidString
//    @Persisted var ownerId = ""
//    @Persisted var userSearchedWord : RealmSwift.List<String>
//    @Persisted var deviceName = UIDevice.current.RealmObject

class UserDataClassRealm() : RealmObject {
    @PrimaryKey
    var _id = RealmUUID.random()
    var ownerId = ""
//    var userSearchedWord : ArrayList<String> = arrayListOf<String>()
    var userSearchedWord : RealmList<String> = realmListOf()

    var deviceName = Build.DEVICE.toString()


}