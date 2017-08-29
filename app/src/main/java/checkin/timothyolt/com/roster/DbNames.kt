package checkin.timothyolt.com.roster

fun Class<*>.dbName() = this.simpleName.toLowerCase()
fun Class<*>.dbNames() = this.dbName().plus(if (this.dbName().last() == 's') "es" else "s")