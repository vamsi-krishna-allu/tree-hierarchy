package com.example.hierarchy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HierarchyTest {

    @Test
    void testFilter() {
        Hierarchy unfiltered = new ArrayBasedHierarchy(
            new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11},
            new int[]{0, 1, 2, 3, 1, 0, 1, 0, 1, 1,  2}
        );
        Hierarchy filtered = HierarchyFilter.filter(unfiltered, nodeId -> nodeId % 3 != 0);
        Hierarchy expected = new ArrayBasedHierarchy(
            new int[]{1, 2, 5, 8, 10, 11},
            new int[]{0, 1, 1, 0,  1,  2}
        );
        assertEquals(expected.formatString(), filtered.formatString());
    }

    @Test
    void testFilterWithNullHierarchy(){
        assertThrows(IllegalArgumentException.class, 
        () -> HierarchyFilter.filter(null, id -> true));
    }

    @Test
    void testFilterWithNullNodePredicate(){
        Hierarchy unfiltered = new ArrayBasedHierarchy(
            new int[]{1, 2, 3, 4, 5, 6},
            new int[]{0, 1, 2, 3, 1, 0}
        );
        assertThrows(IllegalArgumentException.class, 
        () -> HierarchyFilter.filter(unfiltered, null));
    }

    @Test
    void testFilterWithEmptyHierarchy(){
        Hierarchy unfiltered = new ArrayBasedHierarchy(
            new int[]{},
            new int[]{}
        );
        Hierarchy filtered = HierarchyFilter.filter(unfiltered, nodeId -> nodeId % 3 != 0);
        assertEquals("[]", filtered.formatString());
    }

    @Test
    void testFilterWithAllNodesPass(){
        Hierarchy unfiltered = new ArrayBasedHierarchy(
            new int[]{1, 2, 3},
            new int[]{0, 1, 2}
        );
        Hierarchy filtered = HierarchyFilter.filter(unfiltered, nodeId -> true);
        assertEquals(unfiltered.formatString(), filtered.formatString());
    }

    @Test
    void testFilterWithAllNodesFail(){
        Hierarchy unfiltered = new ArrayBasedHierarchy(
            new int[]{1, 2, 3},
            new int[]{0, 1, 2}
        );
        Hierarchy filtered = HierarchyFilter.filter(unfiltered, nodeId -> false);
        assertEquals("[]", filtered.formatString());
    }

    @Test
    void testFilterWithRootFail(){
        Hierarchy unfiltered = new ArrayBasedHierarchy(
            new int[]{1, 2, 3},
            new int[]{0, 1, 2}
        );
        Hierarchy filtered = HierarchyFilter.filter(unfiltered, nodeId -> nodeId != 1);
        assertEquals("[]", filtered.formatString());
    }

    @Test
    void testFilterWithMiddleNodeFailRemovesChildren(){
        Hierarchy unfiltered = new ArrayBasedHierarchy(
            new int[]{1, 2, 3, 4, 5},
            new int[]{0, 1, 2, 1, 2}
        );
        Hierarchy filtered = HierarchyFilter.filter(unfiltered, nodeId -> nodeId != 2);
        Hierarchy expected = new ArrayBasedHierarchy(
            new int[]{1, 4, 5},
            new int[]{0, 1, 2}
        );
        assertEquals(expected.formatString(), filtered.formatString());
    }


    @Test
    void testFilterWithAncestorFailExcludesPassingDescendant(){
        Hierarchy unfiltered = new ArrayBasedHierarchy(
            new int[]{1, 2, 3, 4},
            new int[]{0, 1, 2, 3}
        );
        Hierarchy filtered = HierarchyFilter.filter(unfiltered, nodeId -> nodeId != 2);
        Hierarchy expected = new ArrayBasedHierarchy(
            new int[]{1},
            new int[]{0}
        );
        assertEquals(expected.formatString(), filtered.formatString());
    }

    @Test
    void testFilterWithMultipleRootsOneFailsOthersUnaffected(){
        Hierarchy unfiltered = new ArrayBasedHierarchy(
            new int[]{1, 2, 3, 4, 5, 6},
            new int[]{0, 1, 0, 1, 0, 1}
        );
        Hierarchy filtered = HierarchyFilter.filter(unfiltered, nodeId -> nodeId != 3);
        Hierarchy expected = new ArrayBasedHierarchy(
            new int[]{1, 2, 5, 6},
            new int[]{0, 1, 0, 1}
        );
        assertEquals(expected.formatString(), filtered.formatString());
    }


}
