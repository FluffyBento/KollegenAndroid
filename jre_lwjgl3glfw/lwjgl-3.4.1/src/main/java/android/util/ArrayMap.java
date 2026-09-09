

package android.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;


public final class ArrayMap<K, V> implements Map<K, V> {
    private static final boolean DEBUG = false;
    private static final String TAG = "ArrayMap";

    
    private static final int BASE_SIZE = 4;

    
    private static final int CACHE_SIZE = 10;

    
    static final int[] EMPTY_IMMUTABLE_INTS = new int[0];

    
    public static final ArrayMap EMPTY = new ArrayMap(true);

    
    static Object[] mBaseCache;
    static int mBaseCacheSize;
    static Object[] mTwiceBaseCache;
    static int mTwiceBaseCacheSize;

    int[] mHashes;
    Object[] mArray;
    int mSize;
    MapCollections<K, V> mCollections;

    int indexOf(Object key, int hash) {
        final int N = mSize;

        
        if (N == 0) {
            return ~0;
        }

        int index = ContainerHelpers.binarySearch(mHashes, N, hash);

        
        if (index < 0) {
            return index;
        }

        
        if (key.equals(mArray[index<<1])) {
            return index;
        }

        
        int end;
        for (end = index + 1; end < N && mHashes[end] == hash; end++) {
            if (key.equals(mArray[end << 1])) return end;
        }

        
        for (int i = index - 1; i >= 0 && mHashes[i] == hash; i--) {
            if (key.equals(mArray[i << 1])) return i;
        }

        
        
        
        
        return ~end;
    }

    int indexOfNull() {
        final int N = mSize;

        
        if (N == 0) {
            return ~0;
        }

        int index = ContainerHelpers.binarySearch(mHashes, N, 0);

        
        if (index < 0) {
            return index;
        }

        
        if (null == mArray[index<<1]) {
            return index;
        }

        
        int end;
        for (end = index + 1; end < N && mHashes[end] == 0; end++) {
            if (null == mArray[end << 1]) return end;
        }

        
        for (int i = index - 1; i >= 0 && mHashes[i] == 0; i--) {
            if (null == mArray[i << 1]) return i;
        }

        
        
        
        
        return ~end;
    }

    private void allocArrays(final int size) {
        if (mHashes == EMPTY_IMMUTABLE_INTS) {
            throw new UnsupportedOperationException("ArrayMap is immutable");
        }
        if (size == (BASE_SIZE*2)) {
            synchronized (ArrayMap.class) {
                if (mTwiceBaseCache != null) {
                    final Object[] array = mTwiceBaseCache;
                    mArray = array;
                    mTwiceBaseCache = (Object[])array[0];
                    mHashes = (int[])array[1];
                    array[0] = array[1] = null;
                    mTwiceBaseCacheSize--;
                    if (DEBUG) System.out.println("Retrieving 2x cache " + mHashes
                            + " now have " + mTwiceBaseCacheSize + " entries");
                    return;
                }
            }
        } else if (size == BASE_SIZE) {
            synchronized (ArrayMap.class) {
                if (mBaseCache != null) {
                    final Object[] array = mBaseCache;
                    mArray = array;
                    mBaseCache = (Object[])array[0];
                    mHashes = (int[])array[1];
                    array[0] = array[1] = null;
                    mBaseCacheSize--;
                    if (DEBUG) System.out.println("Retrieving 1x cache " + mHashes
                            + " now have " + mBaseCacheSize + " entries");
                    return;
                }
            }
        }

        mHashes = new int[size];
        mArray = new Object[size<<1];
    }

    private static void freeArrays(final int[] hashes, final Object[] array, final int size) {
        if (hashes.length == (BASE_SIZE*2)) {
            synchronized (ArrayMap.class) {
                if (mTwiceBaseCacheSize < CACHE_SIZE) {
                    array[0] = mTwiceBaseCache;
                    array[1] = hashes;
                    for (int i=(size<<1)-1; i>=2; i--) {
                        array[i] = null;
                    }
                    mTwiceBaseCache = array;
                    mTwiceBaseCacheSize++;
                    if (DEBUG) System.out.println("Storing 2x cache " + array
                            + " now have " + mTwiceBaseCacheSize + " entries");
                }
            }
        } else if (hashes.length == BASE_SIZE) {
            synchronized (ArrayMap.class) {
                if (mBaseCacheSize < CACHE_SIZE) {
                    array[0] = mBaseCache;
                    array[1] = hashes;
                    for (int i=(size<<1)-1; i>=2; i--) {
                        array[i] = null;
                    }
                    mBaseCache = array;
                    mBaseCacheSize++;
                    if (DEBUG) System.out.println("Storing 1x cache " + array
                            + " now have " + mBaseCacheSize + " entries");
                }
            }
        }
    }

    
    public ArrayMap() {
        mHashes = EmptyArray.INT;
        mArray = EmptyArray.OBJECT;
        mSize = 0;
    }

    
    public ArrayMap(int capacity) {
        if (capacity == 0) {
            mHashes = EmptyArray.INT;
            mArray = EmptyArray.OBJECT;
        } else {
            allocArrays(capacity);
        }
        mSize = 0;
    }

    private ArrayMap(boolean immutable) {
        
        
        
        mHashes = immutable ? EMPTY_IMMUTABLE_INTS : EmptyArray.INT;
        mArray = EmptyArray.OBJECT;
        mSize = 0;
    }

    
    public ArrayMap(ArrayMap<K, V> map) {
        this();
        if (map != null) {
            putAll(map);
        }
    }

    
    @Override
    public void clear() {
        if (mSize > 0) {
            freeArrays(mHashes, mArray, mSize);
            mHashes = EmptyArray.INT;
            mArray = EmptyArray.OBJECT;
            mSize = 0;
        }
    }

    
    public void erase() {
        if (mSize > 0) {
            final int N = mSize<<1;
            final Object[] array = mArray;
            for (int i=0; i<N; i++) {
                array[i] = null;
            }
            mSize = 0;
        }
    }

    
    public void ensureCapacity(int minimumCapacity) {
        if (mHashes.length < minimumCapacity) {
            final int[] ohashes = mHashes;
            final Object[] oarray = mArray;
            allocArrays(minimumCapacity);
            if (mSize > 0) {
                System.arraycopy(ohashes, 0, mHashes, 0, mSize);
                System.arraycopy(oarray, 0, mArray, 0, mSize<<1);
            }
            freeArrays(ohashes, oarray, mSize);
        }
    }

    
    @Override
    public boolean containsKey(Object key) {
        return indexOfKey(key) >= 0;
    }

    
    public int indexOfKey(Object key) {
        return key == null ? indexOfNull() : indexOf(key, key.hashCode());
    }

    int indexOfValue(Object value) {
        final int N = mSize*2;
        final Object[] array = mArray;
        if (value == null) {
            for (int i=1; i<N; i+=2) {
                if (array[i] == null) {
                    return i>>1;
                }
            }
        } else {
            for (int i=1; i<N; i+=2) {
                if (value.equals(array[i])) {
                    return i>>1;
                }
            }
        }
        return -1;
    }

    
    @Override
    public boolean containsValue(Object value) {
        return indexOfValue(value) >= 0;
    }

    
    @Override
    public V get(Object key) {
        final int index = indexOfKey(key);
        return index >= 0 ? (V)mArray[(index<<1)+1] : null;
    }

    
    public K keyAt(int index) {
        return (K)mArray[index << 1];
    }

    
    public V valueAt(int index) {
        return (V)mArray[(index << 1) + 1];
    }

    
    public V setValueAt(int index, V value) {
        index = (index << 1) + 1;
        V old = (V)mArray[index];
        mArray[index] = value;
        return old;
    }

    
    @Override
    public boolean isEmpty() {
        return mSize <= 0;
    }

    
    @Override
    public V put(K key, V value) {
        final int hash;
        int index;
        if (key == null) {
            hash = 0;
            index = indexOfNull();
        } else {
            hash = key.hashCode();
            index = indexOf(key, hash);
        }
        if (index >= 0) {
            index = (index<<1) + 1;
            final V old = (V)mArray[index];
            mArray[index] = value;
            return old;
        }

        index = ~index;
        if (mSize >= mHashes.length) {
            final int n = mSize >= (BASE_SIZE*2) ? (mSize+(mSize>>1))
                    : (mSize >= BASE_SIZE ? (BASE_SIZE*2) : BASE_SIZE);

            if (DEBUG) System.out.println("put: grow from " + mHashes.length + " to " + n);

            final int[] ohashes = mHashes;
            final Object[] oarray = mArray;
            allocArrays(n);

            if (mHashes.length > 0) {
                if (DEBUG) System.out.println("put: copy 0-" + mSize + " to 0");
                System.arraycopy(ohashes, 0, mHashes, 0, ohashes.length);
                System.arraycopy(oarray, 0, mArray, 0, oarray.length);
            }

            freeArrays(ohashes, oarray, mSize);
        }

        if (index < mSize) {
            if (DEBUG) System.out.println("put: move " + index + "-" + (mSize-index)
                    + " to " + (index+1));
            System.arraycopy(mHashes, index, mHashes, index + 1, mSize - index);
            System.arraycopy(mArray, index << 1, mArray, (index + 1) << 1, (mSize - index) << 1);
        }

        mHashes[index] = hash;
        mArray[index<<1] = key;
        mArray[(index<<1)+1] = value;
        mSize++;
        return null;
    }

    
    public void append(K key, V value) {
        int index = mSize;
        final int hash = key == null ? 0 : key.hashCode();
        if (index >= mHashes.length) {
            throw new IllegalStateException("Array is full");
        }
        if (index > 0 && mHashes[index-1] > hash) {
            RuntimeException e = new RuntimeException("here");
            e.fillInStackTrace();
            System.out.println("New hash " + hash
                    + " is before end of array hash " + mHashes[index-1]
                    + " at index " + index + " key " + key);
            e.printStackTrace();
            put(key, value);
            return;
        }
        mSize = index+1;
        mHashes[index] = hash;
        index <<= 1;
        mArray[index] = key;
        mArray[index+1] = value;
    }

    
    public void validate() {
        final int N = mSize;
        if (N <= 1) {
            
            return;
        }
        int basehash = mHashes[0];
        int basei = 0;
        for (int i=1; i<N; i++) {
            int hash = mHashes[i];
            if (hash != basehash) {
                basehash = hash;
                basei = i;
                continue;
            }
            
            
            final Object cur = mArray[i<<1];
            for (int j=i-1; j>=basei; j--) {
                final Object prev = mArray[j<<1];
                if (cur == prev) {
                    throw new IllegalArgumentException("Duplicate key in ArrayMap: " + cur);
                }
                if (cur != null && prev != null && cur.equals(prev)) {
                    throw new IllegalArgumentException("Duplicate key in ArrayMap: " + cur);
                }
            }
        }
    }

    
    public void putAll(ArrayMap<? extends K, ? extends V> array) {
        final int N = array.mSize;
        ensureCapacity(mSize + N);
        if (mSize == 0) {
            if (N > 0) {
                System.arraycopy(array.mHashes, 0, mHashes, 0, N);
                System.arraycopy(array.mArray, 0, mArray, 0, N<<1);
                mSize = N;
            }
        } else {
            for (int i=0; i<N; i++) {
                put(array.keyAt(i), array.valueAt(i));
            }
        }
    }

    
    @Override
    public V remove(Object key) {
        final int index = indexOfKey(key);
        if (index >= 0) {
            return removeAt(index);
        }

        return null;
    }

    
    public V removeAt(int index) {
        final Object old = mArray[(index << 1) + 1];
        if (mSize <= 1) {
            
            if (DEBUG) System.out.println("remove: shrink from " + mHashes.length + " to 0");
            freeArrays(mHashes, mArray, mSize);
            mHashes = EmptyArray.INT;
            mArray = EmptyArray.OBJECT;
            mSize = 0;
        } else {
            if (mHashes.length > (BASE_SIZE*2) && mSize < mHashes.length/3) {
                
                
                
                final int n = mSize > (BASE_SIZE*2) ? (mSize + (mSize>>1)) : (BASE_SIZE*2);

                if (DEBUG) System.out.println("remove: shrink from " + mHashes.length + " to " + n);

                final int[] ohashes = mHashes;
                final Object[] oarray = mArray;
                allocArrays(n);

                mSize--;
                if (index > 0) {
                    if (DEBUG) System.out.println("remove: copy from 0-" + index + " to 0");
                    System.arraycopy(ohashes, 0, mHashes, 0, index);
                    System.arraycopy(oarray, 0, mArray, 0, index << 1);
                }
                if (index < mSize) {
                    if (DEBUG) System.out.println("remove: copy from " + (index+1) + "-" + mSize
                            + " to " + index);
                    System.arraycopy(ohashes, index + 1, mHashes, index, mSize - index);
                    System.arraycopy(oarray, (index + 1) << 1, mArray, index << 1,
                            (mSize - index) << 1);
                }
            } else {
                mSize--;
                if (index < mSize) {
                    if (DEBUG) System.out.println("remove: move " + (index+1) + "-" + mSize
                            + " to " + index);
                    System.arraycopy(mHashes, index + 1, mHashes, index, mSize - index);
                    System.arraycopy(mArray, (index + 1) << 1, mArray, index << 1,
                            (mSize - index) << 1);
                }
                mArray[mSize << 1] = null;
                mArray[(mSize << 1) + 1] = null;
            }
        }
        return (V)old;
    }

    
    @Override
    public int size() {
        return mSize;
    }

    
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) object;
            if (size() != map.size()) {
                return false;
            }

            try {
                for (int i=0; i<mSize; i++) {
                    K key = keyAt(i);
                    V mine = valueAt(i);
                    Object theirs = map.get(key);
                    if (mine == null) {
                        if (theirs != null || !map.containsKey(key)) {
                            return false;
                        }
                    } else if (!mine.equals(theirs)) {
                        return false;
                    }
                }
            } catch (NullPointerException ignored) {
                return false;
            } catch (ClassCastException ignored) {
                return false;
            }
            return true;
        }
        return false;
    }

    
    @Override
    public int hashCode() {
        final int[] hashes = mHashes;
        final Object[] array = mArray;
        int result = 0;
        for (int i = 0, v = 1, s = mSize; i < s; i++, v+=2) {
            Object value = array[v];
            result += hashes[i] ^ (value == null ? 0 : value.hashCode());
        }
        return result;
    }

    
    @Override
    public String toString() {
        if (isEmpty()) {
            return "{}";
        }

        StringBuilder buffer = new StringBuilder(mSize * 28);
        buffer.append('{');
        for (int i=0; i<mSize; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            Object key = keyAt(i);
            if (key != this) {
                buffer.append(key);
            } else {
                buffer.append("(this Map)");
            }
            buffer.append('=');
            Object value = valueAt(i);
            if (value != this) {
                buffer.append(value);
            } else {
                buffer.append("(this Map)");
            }
        }
        buffer.append('}');
        return buffer.toString();
    }

    
    
    
    

    private MapCollections<K, V> getCollection() {
        if (mCollections == null) {
            mCollections = new MapCollections<K, V>() {
                @Override
                protected int colGetSize() {
                    return mSize;
                }

                @Override
                protected Object colGetEntry(int index, int offset) {
                    return mArray[(index<<1) + offset];
                }

                @Override
                protected int colIndexOfKey(Object key) {
                    return indexOfKey(key);
                }

                @Override
                protected int colIndexOfValue(Object value) {
                    return indexOfValue(value);
                }

                @Override
                protected Map<K, V> colGetMap() {
                    return ArrayMap.this;
                }

                @Override
                protected void colPut(K key, V value) {
                    put(key, value);
                }

                @Override
                protected V colSetValue(int index, V value) {
                    return setValueAt(index, value);
                }

                @Override
                protected void colRemoveAt(int index) {
                    removeAt(index);
                }

                @Override
                protected void colClear() {
                    clear();
                }
            };
        }
        return mCollections;
    }

    
    public boolean containsAll(Collection<?> collection) {
        return MapCollections.containsAllHelper(this, collection);
    }

    
    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        ensureCapacity(mSize + map.size());
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    
    public boolean removeAll(Collection<?> collection) {
        return MapCollections.removeAllHelper(this, collection);
    }

    
    public boolean retainAll(Collection<?> collection) {
        return MapCollections.retainAllHelper(this, collection);
    }

    
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return getCollection().getEntrySet();
    }

    
    @Override
    public Set<K> keySet() {
        return getCollection().getKeySet();
    }

    
    @Override
    public Collection<V> values() {
        return getCollection().getValues();
    }
}
