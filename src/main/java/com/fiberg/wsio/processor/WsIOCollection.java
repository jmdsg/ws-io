package com.fiberg.wsio.processor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.*;

final class WsIOCollection {

	private WsIOCollection() {}

	static final Map<String, String> IMPLEMENTATIONS = ImmutableMap.<String, String>builder()
		.put(Collection.class.getCanonicalName(), ArrayList.class.getCanonicalName())
		.put(List.class.getCanonicalName(), ArrayList.class.getCanonicalName())
		.put(Queue.class.getCanonicalName(), PriorityQueue.class.getCanonicalName())
		.put(Set.class.getCanonicalName(), HashSet.class.getCanonicalName())
		.put(SortedSet.class.getCanonicalName(), TreeSet.class.getCanonicalName())
		.put(Map.class.getCanonicalName(), HashMap.class.getCanonicalName())
		.put(SortedMap.class.getCanonicalName(), TreeMap.class.getCanonicalName())
		.build();

	static final Set<String> COLLECTION_INTERFACES = ImmutableSet.<String>builder()
			.add(Collection.class.getCanonicalName())
			.add(List.class.getCanonicalName())
			.add(Queue.class.getCanonicalName())
			.add(Set.class.getCanonicalName())
			.add(SortedSet.class.getCanonicalName())
			.build();

	static final Set<String> MAP_INTERFACES = ImmutableSet.<String>builder()
			.add(Map.class.getCanonicalName())
			.add(SortedMap.class.getCanonicalName())
			.build();

	static final Set<String> COLLECTION_CLASSES = ImmutableSet.<String>builder()
			.add(ArrayList.class.getCanonicalName())
			.add(Vector.class.getCanonicalName())
			.add(LinkedList.class.getCanonicalName())
			.add(PriorityQueue.class.getCanonicalName())
			.add(HashSet.class.getCanonicalName())
			.add(LinkedHashSet.class.getCanonicalName())
			.add(TreeSet.class.getCanonicalName())
			.build();

	static final Set<String> MAP_CLASSES = ImmutableSet.<String>builder()
			.add(HashMap.class.getCanonicalName())
			.add(LinkedHashMap.class.getCanonicalName())
			.add(Hashtable.class.getCanonicalName())
			.add(TreeMap.class.getCanonicalName())
			.build();

	static final Set<String> MAPS = ImmutableSet.<String>builder()
			.addAll(MAP_INTERFACES)
			.addAll(MAP_CLASSES)
			.build();

	static final Set<String> COLLECTIONS = ImmutableSet.<String>builder()
			.addAll(COLLECTION_INTERFACES)
			.addAll(COLLECTION_CLASSES)
			.build();

	static final Set<String> ALL = ImmutableSet.<String>builder()
			.addAll(MAPS)
			.addAll(COLLECTIONS)
			.build();

}
