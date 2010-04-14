/**
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
package org.fusesource.hawtdb.api;

import org.fusesource.hawtdb.internal.index.BTreeIndex;
import org.fusesource.hawtdb.util.marshaller.Marshaller;
import org.fusesource.hawtdb.util.marshaller.ObjectMarshaller;

/**
 * This object is used to create variable magnitude b+tree indexes. 
 * 
 * A b+tree can be used for set or map-based indexing. Leaf
 * nodes are linked together for faster iteration of the values.
 * 
 * <br>
 * The variable magnitude attribute means that the b+tree attempts 
 * to store as many values and pointers on one page as is possible.
 * 
 * <br>
 * It will act as a simple-prefix b+tree if a prefixer is configured.
 * 
 * <br>
 * In a simple-prefix b+tree, instead of promoting actual keys to branch pages, when
 * leaves are split, a shortest-possible separator is generated at the pivot.
 * That separator is what is promoted to the parent branch (and continuing up
 * the list). As a result, actual keys and pointers can only be found at the
 * leaf level. This also affords the index the ability to ignore costly merging
 * and redistribution of pages when deletions occur. Deletions only affect leaf
 * pages in this implementation, and so it is entirely possible for a leaf page
 * to be completely empty after all of its keys have been removed.
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class BTreeIndexFactory<Key, Value> implements IndexFactory<Key, Value> {

    private Marshaller<Key> keyMarshaller = new ObjectMarshaller<Key>();
    private Marshaller<Value> valueMarshaller = new ObjectMarshaller<Value>();
    private boolean deferredEncoding=true;
    private Prefixer<Key> prefixer;

    /**
     * Creates a new BTree index on the Paged object at the given page location.
     */
    public SortedIndex<Key, Value> create(Paged paged, int page) {
        BTreeIndex<Key, Value> index = createInstance(paged, page);
        index.create();
        return index;
    }
    
    @Override
    public String toString() {
        return "{ deferredEncoding: "+deferredEncoding+" }";
    }
    
    /**
     * Loads an existing BTree index from the paged object at the given page location.
     */
    public SortedIndex<Key, Value> open(Paged paged, int page) {
        return createInstance(paged, page);
    }

    private BTreeIndex<Key, Value> createInstance(Paged paged, int page) {
        return new BTreeIndex<Key, Value>(paged, page, this);
    }

    /**
     * Defaults to an {@link ObjectMarshaller} if not explicitly set.
     * 
     * @return the marshaller used for keys.
     */
    public Marshaller<Key> getKeyMarshaller() {
        return keyMarshaller;
    }

    /**
     * Allows you to configure custom marshalling logic to encode the index keys.
     * 
     * @param marshaller the marshaller used for keys.
     */
    public void setKeyMarshaller(Marshaller<Key> marshaller) {
        this.keyMarshaller = marshaller;
    }

    /**
     * Defaults to an {@link ObjectMarshaller} if not explicitly set.
     *  
     * @return the marshaller used for values.
     */
    public Marshaller<Value> getValueMarshaller() {
        return valueMarshaller;
    }

    /**
     * Allows you to configure custom marshalling logic to encode the index values.
     * 
     * @param marshaller the marshaller used for values
     */
    public void setValueMarshaller(Marshaller<Value> marshaller) {
        this.valueMarshaller = marshaller;
    }

    /**
     * 
     * @return true if deferred encoding enabled
     */
    public boolean isDeferredEncoding() {
        return deferredEncoding;
    }

    /**
     * <p>
     * When deferred encoding is enabled, the index avoids encoding keys and values
     * for as long as possible so take advantage of collapsing multiple updates of the 
     * same key/value into a single update operation and single encoding operation.
     * </p><p>
     * Using this feature requires the keys and values to be immutable objects since 
     * unexpected errors would occur if they are changed after they have been handed
     * to to the index for storage. 
     * </p>
     * @param enable should deferred encoding be enabled.
     */
    public void setDeferredEncoding(boolean enable) {
        this.deferredEncoding = enable;
    }

    public Prefixer<Key> getPrefixer() {
        return prefixer;
    }

    public void setPrefixer(Prefixer<Key> prefixer) {
        this.prefixer = prefixer;
    }
    
}