package com.orientechnologies.orient.core.storage.impl.local.paginated.wal.po.cluster.v1.paginatedclusterstate;

import com.orientechnologies.common.directmemory.OByteBufferPool;
import com.orientechnologies.common.directmemory.OPointer;
import com.orientechnologies.orient.core.storage.cache.OCacheEntry;
import com.orientechnologies.orient.core.storage.cache.OCacheEntryImpl;
import com.orientechnologies.orient.core.storage.cache.OCachePointer;
import com.orientechnologies.orient.core.storage.cluster.OClusterPage;
import com.orientechnologies.orient.core.storage.cluster.v1.OPaginatedClusterStateV1;
import com.orientechnologies.orient.core.storage.impl.local.paginated.wal.po.PageOperationRecord;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.List;

public class PaginatedClusterStateV1SetFreeListPagePOTest {
  @Test
  public void testRedo() {
    final int pageSize = OClusterPage.PAGE_SIZE;
    final OByteBufferPool byteBufferPool = new OByteBufferPool(pageSize);
    try {
      final OPointer pointer = byteBufferPool.acquireDirect(false);
      final OCachePointer cachePointer = new OCachePointer(pointer, byteBufferPool, 0, 0);
      final OCacheEntry entry = new OCacheEntryImpl(0, 0, cachePointer);

      OPaginatedClusterStateV1 clusterState = new OPaginatedClusterStateV1(entry);
      clusterState.setFreeListPage(0, 12);
      clusterState.setFreeListPage(1, 14);
      clusterState.setFreeListPage(2, 16);

      entry.clearPageOperations();

      final OPointer restoredPointer = byteBufferPool.acquireDirect(false);
      final OCachePointer restoredCachePointer = new OCachePointer(restoredPointer, byteBufferPool, 0, 0);
      final OCacheEntry restoredCacheEntry = new OCacheEntryImpl(0, 0, restoredCachePointer);

      final ByteBuffer originalBuffer = cachePointer.getBufferDuplicate();
      final ByteBuffer restoredBuffer = restoredCachePointer.getBufferDuplicate();

      Assert.assertNotNull(originalBuffer);
      Assert.assertNotNull(restoredBuffer);

      restoredBuffer.put(originalBuffer);

      clusterState.setFreeListPage(1, 42);

      final List<PageOperationRecord> operations = entry.getPageOperations();
      Assert.assertEquals(1, operations.size());

      Assert.assertTrue(operations.get(0) instanceof PaginatedClusterStateV1SetFreeListPagePO);
      final PaginatedClusterStateV1SetFreeListPagePO pageOperation = (PaginatedClusterStateV1SetFreeListPagePO) operations.get(0);

      OPaginatedClusterStateV1 restoredPage = new OPaginatedClusterStateV1(restoredCacheEntry);

      Assert.assertEquals(12, restoredPage.getFreeListPage(0));
      Assert.assertEquals(14, restoredPage.getFreeListPage(1));
      Assert.assertEquals(16, restoredPage.getFreeListPage(2));

      pageOperation.redo(restoredCacheEntry);

      Assert.assertEquals(12, restoredPage.getFreeListPage(0));
      Assert.assertEquals(42, restoredPage.getFreeListPage(1));
      Assert.assertEquals(16, restoredPage.getFreeListPage(2));

      byteBufferPool.release(pointer);
      byteBufferPool.release(restoredPointer);
    } finally {
      byteBufferPool.clear();
    }
  }

  @Test
  public void testUndo() {
    final int pageSize = OClusterPage.PAGE_SIZE;

    final OByteBufferPool byteBufferPool = new OByteBufferPool(pageSize);
    try {
      final OPointer pointer = byteBufferPool.acquireDirect(false);
      final OCachePointer cachePointer = new OCachePointer(pointer, byteBufferPool, 0, 0);
      final OCacheEntry entry = new OCacheEntryImpl(0, 0, cachePointer);

      OPaginatedClusterStateV1 clusterState = new OPaginatedClusterStateV1(entry);
      clusterState.setFreeListPage(0, 12);
      clusterState.setFreeListPage(1, 14);
      clusterState.setFreeListPage(2, 16);

      entry.clearPageOperations();

      clusterState.setFreeListPage(1, 42);

      final List<PageOperationRecord> operations = entry.getPageOperations();
      Assert.assertEquals(1, operations.size());

      Assert.assertTrue(operations.get(0) instanceof PaginatedClusterStateV1SetFreeListPagePO);

      final PaginatedClusterStateV1SetFreeListPagePO pageOperation = (PaginatedClusterStateV1SetFreeListPagePO) operations.get(0);

      final OPaginatedClusterStateV1 restoredPage = new OPaginatedClusterStateV1(entry);

      Assert.assertEquals(12, restoredPage.getFreeListPage(0));
      Assert.assertEquals(42, restoredPage.getFreeListPage(1));
      Assert.assertEquals(16, restoredPage.getFreeListPage(2));

      pageOperation.undo(entry);

      Assert.assertEquals(12, restoredPage.getFreeListPage(0));
      Assert.assertEquals(14, restoredPage.getFreeListPage(1));
      Assert.assertEquals(16, restoredPage.getFreeListPage(2));

      byteBufferPool.release(pointer);
    } finally {
      byteBufferPool.clear();
    }
  }

  @Test
  public void testSerialization() {
    PaginatedClusterStateV1SetFreeListPagePO operation = new PaginatedClusterStateV1SetFreeListPagePO(12, 42, 24);

    operation.setFileId(42);
    operation.setPageIndex(24);
    operation.setOperationUnitId(1);

    final int serializedSize = operation.serializedSize();
    final byte[] stream = new byte[serializedSize + 1];
    int pos = operation.toStream(stream, 1);

    Assert.assertEquals(serializedSize + 1, pos);

    PaginatedClusterStateV1SetFreeListPagePO restoredOperation = new PaginatedClusterStateV1SetFreeListPagePO();
    restoredOperation.fromStream(stream, 1);

    Assert.assertEquals(42, restoredOperation.getFileId());
    Assert.assertEquals(24, restoredOperation.getPageIndex());
    Assert.assertEquals(1, restoredOperation.getOperationUnitId());

    Assert.assertEquals(12, restoredOperation.getIndex());
    Assert.assertEquals(42, restoredOperation.getOldPageIndex());
    Assert.assertEquals(24, restoredOperation.getNewPageIndex());
  }

}
