package sensors_in_paradise.sonar

import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.*
import org.junit.Before
import org.junit.Test
import sensors_in_paradise.sonar.uploader.LocalOwnCloudMetadataStorage
import java.io.File

class OwnCloudMetadataStorageTest {
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    val baseDir = File(appContext.cacheDir, "RecordingsTest")
    @Before
    fun init() {
        baseDir.mkdir()
    }

    @Test
    fun simpleFileSettersTest() {
        val storage = LocalOwnCloudMetadataStorage(appContext, baseDir)
        val testFile = File(baseDir, "test.txt")
        assertFalse(storage.isFileUploaded(testFile))
        storage.setFileUploaded(testFile)
        assertTrue(storage.isFileUploaded(testFile))
        storage.file.delete()
    }
    @Test
    fun deepFileSettersTest() {
        val storage = LocalOwnCloudMetadataStorage(appContext, baseDir)
        val testFile = File(baseDir.resolve("fancyDir").resolve("deeperDir"), "test.txt")
        assertFalse(storage.isFileUploaded(testFile))
        storage.setFileUploaded(testFile)
        assertTrue(storage.isFileUploaded(testFile))
        storage.file.delete()
    }
    @Test
    fun simpleDirSettersTest() {
        val storage = LocalOwnCloudMetadataStorage(appContext, baseDir)
        val testDir = File(baseDir, "test")

        assertFalse(storage.isDirCreated(testDir))
        storage.setDirCreated(testDir)
        assertTrue(storage.isDirCreated(testDir))
        storage.file.delete()
    }
    @Test
    fun deepDirSettersTest() {
        val storage = LocalOwnCloudMetadataStorage(appContext, baseDir)
        val testDir = File(baseDir.resolve("childA").resolve("deepChild"), "deepestChild")

        assertFalse(storage.isDirCreated(testDir))
        storage.setDirCreated(testDir)
        assertTrue(storage.isDirCreated(testDir))
        storage.file.delete()
    }
    @Test
    fun storageTest() {
        val storageA = LocalOwnCloudMetadataStorage(appContext, baseDir)
        val testDir = File(baseDir.resolve("childA").resolve("deepChild"), "deepestChild")

        assertFalse(storageA.isDirCreated(testDir))
        storageA.setDirCreated(testDir)

        val testFile = File(baseDir.resolve("fancyDir").resolve("deeperDir"), "test.txt")
        assertFalse(storageA.isFileUploaded(testFile))
        storageA.setFileUploaded(testFile)

        val storageB = LocalOwnCloudMetadataStorage(appContext, baseDir)
        assertTrue(storageB.isDirCreated(testDir))
        assertTrue(storageB.isFileUploaded(testFile))

        storageB.file.delete()
    }
}
