package com.googlecode.totallylazy.collections;

import com.googlecode.totallylazy.functions.Function1;
import com.googlecode.totallylazy.functions.Function2;
import com.googlecode.totallylazy.functions.Functions;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.predicates.Predicate;
import com.googlecode.totallylazy.predicates.Predicates;
import com.googlecode.totallylazy.Segment;
import com.googlecode.totallylazy.Unchecked;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.googlecode.totallylazy.functions.Functions.call;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Unchecked.cast;

public abstract class AbstractTreeMap<K, V, Self extends TreeMap<K, V>> extends AbstractMap<K,V> implements TreeMap<K, V> {
    protected final Comparator<K> comparator;
    protected final K key;
    protected final V value;
    protected final Self left;
    protected final Self right;
    protected final TreeFactory factory;
    protected final int size;

    protected AbstractTreeMap(Comparator<K> comparator, K key, V value, Self left, Self right, TreeFactory factory) {
        this.comparator = comparator;
        this.key = key;
        this.value = value;
        this.left = left;
        this.right = right;
        this.factory = factory;
        size = left.size() + right.size() + 1;
    }

    @Override
    public TreeFactory factory() {
        return factory;
    }

    @Override
    public Comparator<K> comparator() {
        return comparator;
    }

    @Override
    public K key() {
        return key;
    }

    @Override
    public V value() {
        return value;
    }

    @Override
    public Self left() {
        return left;
    }

    @Override
    public Self left(TreeMap<K, V> newLeft) {
        return cast(factory.create(comparator, key, value, newLeft, right()));
    }

    @Override
    public Self right() {
        return right;
    }

    @Override
    public Self right(TreeMap<K, V> newRight) {
        return cast(factory.create(comparator, key, value, left(), newRight));
    }

    @Override
    public Self rotateLeft() {
        TreeMap<K, V> b = right.left();
        Self three = right(b);
        return cast(right.left(three));
    }

    @Override
    public Self rotateRight() {
        TreeMap<K, V> c = left().right();
        Self five = left(c);
        return cast(left().right(five));
    }

    @Override
    public PersistentList<Pair<K, V>> toPersistentList() {
        return joinTo(PersistentList.constructors.<Pair<K, V>>empty());
    }

    @Override
    public Option<V> lookup(K other) {
        int difference = difference(other);
        if (difference == 0) return Option.option(value);
        if (difference < 0) return left.lookup(other);
        return right.lookup(other);
    }

    @Override
    public Self insert(K key, V value) {
        int difference = difference(key);
        if (difference == 0) return create(comparator, key, value, left, right);
        if (difference < 0) return create(comparator, this.key, this.value, left.insert(key, value), right);
        return create(comparator, this.key, this.value, left, right.insert(key, value));
    }

    @Override
    public Option<V> find(Predicate<? super K> predicate) {
        if (predicate.matches(key)) return some(value);
        Option<V> left = this.left.find(predicate);
        if (left.isEmpty()) return right.find(predicate);
        return left;
    }

    @Override
    public Self filter(Predicate<? super Pair<K, V>> predicate) {
        if (predicate.matches(pair()))
            return create(comparator, key, value, left.filter(predicate), right.filter(predicate));
        return left.filter(predicate).joinTo(self(right.filter(predicate)));
    }

    @Override
    public Self filterKeys(final Predicate<? super K> predicate) {
        return filter(Predicates.<K>first(predicate));
    }

    @Override
    public Self filterValues(final Predicate<? super V> predicate) {
        return filter(Predicates.<V>second(predicate));
    }

    @Override
    public <S> S fold(S seed, Function2<? super S, ? super Pair<K, V>, ? extends S> callable) {
        return right.fold(left.fold(Functions.call(callable, seed, pair()), callable), callable);
    }

    @Override
    public <NewV> TreeMap<K, NewV> map(Function1<? super V, ? extends NewV> transformer) {
        return TreeMap.methods.map(transformer, factory, this);
    }

    @Override
    public Self delete(K key) {
        int difference = difference(key);
        if (difference == 0) {
            if (left.isEmpty()) return right;
            if (right.isEmpty()) return left;
            Pair<? extends TreeMap<K, V>, Pair<K, V>> pair = left.removeLast();
            TreeMap<K, V> newLeft = pair.first();
            Pair<K, V> newRoot = pair.second();
            return create(comparator, newRoot.first(), newRoot.second(), newLeft, right);
        }
        if (difference < 0) return create(comparator, this.key, value, left.delete(key), right);
        return create(comparator, this.key, value, left, right.delete(key));
    }

    @Override
    public Pair<K, V> first() throws NoSuchElementException {
        if (left.isEmpty()) return pair();
        return left.first();
    }

    @Override
    public Pair<K, V> last() throws NoSuchElementException {
        if (right.isEmpty()) return pair();
        return right.last();
    }

    @Override
    public Pair<Self, Pair<K, V>> removeFirst() {
        if (left.isEmpty()) return Pair.pair(right, pair());
        final Pair<? extends TreeMap<K, V>, Pair<K, V>> newLeft = left.removeFirst();
        return Pair.pair(create(comparator, key, value, newLeft.first(), right), newLeft.second());
    }

    @Override
    public Pair<Self, Pair<K, V>> removeLast() {
        if (right.isEmpty()) return Pair.pair(left, pair());
        final Pair<? extends TreeMap<K, V>, Pair<K, V>> newRight = right.removeLast();
        return Pair.pair(create(comparator, key, value, left, newRight.first()), newRight.second());
    }

    @Override
    public <C extends Segment<Pair<K, V>>> C joinTo(C rest) {
        return cast(left.joinTo(right.joinTo(rest).cons(pair())));
    }

    @Override
    public Self cons(Pair<K, V> newValue) {
        return insert(newValue.first(), newValue.second());
    }

    @Override
    public boolean contains(Object other) {
        int difference = difference(Unchecked.<K>cast(other));
        if (difference == 0) return true;
        if (difference < 0) return left.contains(other);
        return right.contains(other);
    }

    @Override
    public boolean exists(Predicate<? super K> predicate) {
        return predicate.matches(key) || left.exists(predicate) || right.exists(predicate);
    }

    @Override
    public int hashCode() {
        return 19 * value.hashCode() * left.hashCode() * right.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AbstractTreeMap && value.equals(((AbstractTreeMap) obj).value) && left.equals(((AbstractTreeMap) obj).left) && right.equals(((AbstractTreeMap) obj).right);
    }

    @Override
    public String toString() {
        return String.format("(%s %s=%s %s)", left, key, value, right);
    }

    @Override
    public Self empty() {
        return self(factory.<K,V>create(comparator));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Pair<K, V> head() throws NoSuchElementException {
        return pair();
    }

    @Override
    public Option<Pair<K, V>> headOption() {
        return some(head());
    }

    @Override
    public Self tail() throws NoSuchElementException {
        return left.joinTo(right);
    }

    @Override
    public Iterator<Pair<K, V>> iterator() {
        return new TreeIterator<K, V>(this);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Pair<K, V> get(int i) {
        if (left.size() == i) return pair();
        if (i < left.size()) return left.get(i);
        return right.get(i - left.size() - 1);
    }

    @Override
    public int indexOf(Object pair) {
        int difference = difference(Unchecked.<Pair<K,V>>cast(pair).first());
        if (difference == 0) return left.size();
        if (difference < 0) return left.indexOf(pair);
        return 1 + left.size() + right.indexOf(pair);
    }

    protected Pair<K, V> pair() {
        return Pair.pair(key, value);
    }

    protected int difference(K key) {
        return comparator.compare(key, this.key);
    }

    protected Self self(TreeMap<K, V> treeMap) {
        return cast(treeMap);
    }

    protected Self create(Comparator<K> comparator, K key, V value, TreeMap<K, V> left, TreeMap<K, V> right) {
        return self(factory.create(comparator, key, value, left, right));
    }
}