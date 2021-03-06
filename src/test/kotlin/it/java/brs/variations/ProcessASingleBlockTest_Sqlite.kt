package it.java.brs.variations

import brs.common.TestInfrastructure
import it.java.brs.ProcessASingleBlockTest

class ProcessASingleBlockTest_Sqlite : ProcessASingleBlockTest() {
    override fun getDbUrl(): String {
        return TestInfrastructure.IN_MEMORY_SQLITE_DB_URL
    }
}
