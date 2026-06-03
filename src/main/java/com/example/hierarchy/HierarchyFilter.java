package com.example.hierarchy;

import java.util.Arrays;
import java.util.function.IntPredicate;

/**
 * Utility class for filtering a {@link Hierarchy}.
 *
 * <p>A node is present in the filtered hierarchy iff its node ID passes the predicate
 * and all of its ancestors pass it as well.
 */
public class HierarchyFilter {
    private HierarchyFilter() {}

    public static Hierarchy filter(Hierarchy hierarchy, IntPredicate nodeIdPredicate) {
        if(hierarchy == null){
            throw new IllegalArgumentException("hierarchy should not be null");
        }

        if (nodeIdPredicate == null) {
            throw new IllegalArgumentException("nodeIdPredicate must not be null");
        }
        
        int sizeOfHierarchy = hierarchy.size();
        int blockedDepth = Integer.MAX_VALUE;

        int[] filteredDataIds = new int[sizeOfHierarchy];
        int[] filteredDataDepths  = new int[sizeOfHierarchy];

        int count = 0;

        for (int i = 0; i < sizeOfHierarchy; i++) {
            int id    = hierarchy.nodeId(i);
            int depth = hierarchy.depth(i);

            if (depth > blockedDepth) {
                continue;
            } 
            
            blockedDepth = Integer.MAX_VALUE;
            
            if (nodeIdPredicate.test(id)) {
                filteredDataIds[count] = id;
                filteredDataDepths[count] = depth;
                count++;
            } else {
                blockedDepth = depth;
            }
        }

        int[] nodeIds = Arrays.copyOf(filteredDataIds, count);
        int[] depths  = Arrays.copyOf(filteredDataDepths, count);
        return new ArrayBasedHierarchy(nodeIds, depths);
    }
}
