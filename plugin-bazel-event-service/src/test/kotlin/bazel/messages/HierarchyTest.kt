package bazel.messages

import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class HierarchyTest {
    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun shouldNotOverwriteExistingNode() {
        val hierarchy = Hierarchy()
        hierarchy.createNode(createTestProgressId(1), listOf(), "original node")
        hierarchy.createNode(createTestProgressId(1), listOf(), "replacement node")
        val node = hierarchy.tryCloseNode(createTestProgressId(1))
        assertEquals(node!!.description, "original node")
    }

    @Test
    fun shouldRemoveSingletonNodeWithoutDoingAction() {
        var actionPerformed = false
        val hierarchy = Hierarchy()
        hierarchy.createNode(createTestProgressId(1), listOf(), "original node") {
            actionPerformed = true
        }

        val firstNode = hierarchy.tryCloseNode(createTestProgressId(1))
        assertFalse(actionPerformed)
        assertEquals(firstNode!!.description, "original node")

        // Verify that the original node has been removed
        hierarchy.createNode(createTestProgressId(1), listOf(), "replacement node")
        val secondNode = hierarchy.tryCloseNode(createTestProgressId(1))
        assertEquals(secondNode!!.description, "replacement node")
    }

    @Test
    fun shouldNotRemoveNodeWithChildren() {
        var actionPerformed = false
        val hierarchy = Hierarchy()
        hierarchy.createNode(createTestProgressId(1), listOf(createTestProgressId(2)), "foo") {
            actionPerformed = true
        }

        val firstNode = hierarchy.tryCloseNode(createTestProgressId(1))
        assertFalse(actionPerformed)
        assertEquals(firstNode!!.description, "foo")

        val secondNode = hierarchy.tryCloseNode(createTestProgressId(2))
        assertTrue(actionPerformed)
        // Verify that the child has inherited the parent's description
        assertEquals(secondNode!!.description, "foo")
    }

    @Test
    fun shouldRemoveParentWhenChildIsClosed() {
        var actionPerformed = false
        val hierarchy = Hierarchy()
        hierarchy.createNode(createTestProgressId(2), listOf(), "leaf")
        hierarchy.createNode(createTestProgressId(1), listOf(createTestProgressId(2)), "root") {
            actionPerformed = true
        }

        val firstNode = hierarchy.tryCloseNode(createTestProgressId(1))
        assertFalse(actionPerformed)
        assertEquals(firstNode!!.description, "root")

        val secondNode = hierarchy.tryCloseNode(createTestProgressId(2))
        assertTrue(actionPerformed)
        assertEquals(secondNode!!.description, "leaf")

        // Verify that the root has also been removed
        hierarchy.createNode(createTestProgressId(1), listOf(), "replacement node")
        val thirdNode = hierarchy.tryCloseNode(createTestProgressId(1))
        assertEquals(thirdNode!!.description, "replacement node")
    }

    private fun createTestProgressId(value: Int) =
        BuildEventStreamProtos
            .BuildEventId
            .newBuilder()
            .setTestProgress(
                BuildEventStreamProtos.BuildEventId.TestProgressId
                    .newBuilder()
                    .setLabel(value.toString()),
            ).build()
}
