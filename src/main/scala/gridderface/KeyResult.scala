package gridderface

sealed abstract class KeyResult

object KeyUndefined extends KeyResult
object KeyComplete extends KeyResult
object KeyIncomplete extends KeyResult
case class KeyCompleteWith(status: Status[String]) extends KeyResult
