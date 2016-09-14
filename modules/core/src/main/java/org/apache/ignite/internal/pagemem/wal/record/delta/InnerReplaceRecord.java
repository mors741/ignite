/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.pagemem.wal.record.delta;

import java.nio.ByteBuffer;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.internal.pagemem.Page;
import org.apache.ignite.internal.pagemem.PageMemory;
import org.apache.ignite.internal.processors.cache.database.tree.io.BPlusIO;
import org.apache.ignite.internal.processors.cache.database.tree.io.PageIO;

/**
 * Inner replace on remove.
 */
public class InnerReplaceRecord<L> extends PageDeltaRecord {
    /** */
    private int dstIdx;

    /** */
    private long srcPageId;

    /** */
    private int srcIdx;

    /**
     * @param cacheId Cache ID.
     * @param pageId  Page ID.
     * @param dstIdx Destination index.
     * @param srcPageId Source page ID.
     * @param srcIdx Source index.
     */
    public InnerReplaceRecord(int cacheId, long pageId, int dstIdx, long srcPageId, int srcIdx) {
        super(cacheId, pageId);

        this.dstIdx = dstIdx;
        this.srcPageId = srcPageId;
        this.srcIdx = srcIdx;
    }

    /** {@inheritDoc} */
    @Override public void applyDelta(PageMemory pageMem, ByteBuffer dstBuf) throws IgniteCheckedException {
        BPlusIO<L> io = PageIO.getBPlusIO(dstBuf);

        try (Page src = pageMem.page(cacheId(), srcPageId)) {
            ByteBuffer srcBuf = src.getForRead();

            try {
                BPlusIO<L> srcIo = PageIO.getBPlusIO(srcBuf);

                io.store(dstBuf, dstIdx, srcIo, srcBuf, srcIdx);
            }
            finally {
                src.releaseRead();
            }
        }
    }

    /** {@inheritDoc} */
    @Override public RecordType type() {
        return RecordType.BTREE_PAGE_INNER_REPLACE;
    }

    public int destinationIndex() {
        return dstIdx;
    }

    public long sourcePageId() {
        return srcPageId;
    }

    public int sourceIndex() {
        return srcIdx;
    }
}
