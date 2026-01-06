package com.aldaz.fernando.duckhunt

data class Player (var username:String, var huntedDucks:Int){
    constructor():this("",0)
}
