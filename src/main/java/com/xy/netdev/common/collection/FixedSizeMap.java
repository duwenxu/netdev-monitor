package com.xy.netdev.common.collection;



import java.util.TreeMap;

/**
 * @功能:固定长度MAP
 * @author tangxl
 * @since 2020-09-07
 */
public class FixedSizeMap<K,V>   {

    private TreeMap<K,V> map = new TreeMap<>();

    private int capacity = 10;


    public FixedSizeMap(){
    }

    public FixedSizeMap(int capacity){
        this.capacity = capacity;
    }

    public void put(K key,V value){
        map.put(key,value);
        if(map.size()>capacity){
            map.remove(map.firstKey());
        }
    }

    public V get(K key){
        return map.get(key);
    }

    public void clear(){
        map.clear();
    }

    public int getCapacity(){
        return capacity;
    }

    public boolean containsKey(K key){
        return map.containsKey(key);
    }
}
