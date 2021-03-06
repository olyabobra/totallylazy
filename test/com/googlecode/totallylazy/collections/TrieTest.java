package com.googlecode.totallylazy.collections;

import org.junit.Test;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Segment.constructors.characters;
import static com.googlecode.totallylazy.collections.PersistentSortedMap.constructors;
import static com.googlecode.totallylazy.collections.Trie.trie;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TrieTest {
    @Test
    public void get() throws Exception {
        Trie<Character, String> trie = trie(none(String.class), constructors.<Character, Trie<Character, String>>sortedMap('a', trie(none(String.class), constructors.<Character, Trie<Character, String>>sortedMap('b', trie(some("Foo"), constructors.<Character, Trie<Character, String>>emptySortedMap())))));
        assertThat(trie.get(characters("ab")).get(), is("Foo"));
    }

    @Test
    public void put() throws Exception {
        Trie<Character, String> trie = Trie.<Character, String>trie().put(characters("ab"), "Foo");
        assertThat(trie.get(characters("ab")).get(), is("Foo"));
    }

    @Test
    public void contains() throws Exception {
        Trie<Character, String> trie = Trie.<Character, String>trie().put(characters("ab"), "Foo");
        assertThat(trie.contains(characters("ab")), is(true));
        assertThat(trie.contains(characters("a")), is(false));
        assertThat(trie.contains(characters("b")), is(false));
        assertThat(trie.contains(characters("")), is(false));
    }

    @Test
    public void remove() throws Exception {
        Trie<Character, String> trie = Trie.<Character, String>trie().put(characters("ab"), "Foo").put(characters("aa"), "Bar").remove(characters("ab"));
        assertThat(trie.contains(characters("ab")), is(false));
        assertThat(trie.contains(characters("aa")), is(true));
    }
}
