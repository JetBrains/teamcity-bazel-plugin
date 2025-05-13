package bazel.messages

import bazel.bazel.events.Id
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class HierarchyImplTest {
    @MockK private lateinit var ctx: ServiceMessageContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun shouldNotOverwriteExistingNode() {
        val hierarchy = HierarchyImpl()
        hierarchy.createNode(Id(1), listOf(), "original node")
        hierarchy.createNode(Id(1), listOf(), "replacement node")
        val node = hierarchy.tryCloseNode(ctx, Id(1))
        assertEquals(node!!.description, "original node")
    }

    @Test
    fun shouldRemoveSingletonNodeWithoutDoingAction() {
        var actionPerformed = false
        val hierarchy = HierarchyImpl()
        hierarchy.createNode(Id(1), listOf(), "original node") {
            actionPerformed = true
        }

        val firstNode = hierarchy.tryCloseNode(ctx, Id(1))
        assertFalse(actionPerformed)
        assertEquals(firstNode!!.description, "original node")

        // Verify that the original node has been removed
        hierarchy.createNode(Id(1), listOf(), "replacement node")
        val secondNode = hierarchy.tryCloseNode(ctx, Id(1))
        assertEquals(secondNode!!.description, "replacement node")
    }

    @Test
    fun shouldNotRemoveNodeWithChildren() {
        var actionPerformed = false
        val hierarchy = HierarchyImpl()
        hierarchy.createNode(Id(1), listOf(Id(2)), "foo") {
            actionPerformed = true
        }

        val firstNode = hierarchy.tryCloseNode(ctx, Id(1))
        assertFalse(actionPerformed)
        assertEquals(firstNode!!.description, "foo")

        val secondNode = hierarchy.tryCloseNode(ctx, Id(2))
        assertTrue(actionPerformed)
        // Verify that the child has inherited the parent's description
        assertEquals(secondNode!!.description, "foo")
    }

    @Test
    fun shouldRemoveParentWhenChildIsClosed() {
        var actionPerformed = false
        val hierarchy = HierarchyImpl()
        hierarchy.createNode(Id(2), listOf(), "leaf")
        hierarchy.createNode(Id(1), listOf(Id(2)), "root") {
            actionPerformed = true
        }

        val firstNode = hierarchy.tryCloseNode(ctx, Id(1))
        assertFalse(actionPerformed)
        assertEquals(firstNode!!.description, "root")

        val secondNode = hierarchy.tryCloseNode(ctx, Id(2))
        assertTrue(actionPerformed)
        assertEquals(secondNode!!.description, "leaf")

        // Verify that the root has also been removed
        hierarchy.createNode(Id(1), listOf(), "replacement node")
        val thirdNode = hierarchy.tryCloseNode(ctx, Id(1))
        assertEquals(thirdNode!!.description, "replacement node")
    }
}
