/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.yavi.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConstraintViolations implements List<ConstraintViolation> {
	private final List<ConstraintViolation> delegate;

	/**
	 * Constructs an empty list with an initial capacity of ten.
	 */
	public ConstraintViolations() {
		this.delegate = new ArrayList<>();
	}

	/**
	 * Constructs with the constraintViolations to delegate
	 * @param delegate constraintViolations to delegate
	 * @since 0.6.0
	 */
	public ConstraintViolations(List<ConstraintViolation> delegate) {
		this.delegate = delegate;
	}

	/**
	 * Appends the specified element to the end of this list (optional operation).
	 *
	 * <p>
	 * Lists that support this operation may place limitations on what elements may be
	 * added to this list. In particular, some lists will refuse to add null elements, and
	 * others will impose restrictions on the type of elements that may be added. List
	 * classes should clearly specify in their documentation any restrictions on what
	 * elements may be added.
	 *
	 * @param constraintViolation element to be appended to this list
	 * @return <tt>true</tt> (as specified by {@link Collection#add})
	 * @throws UnsupportedOperationException if the <tt>add</tt> operation is not
	 *     supported by this list
	 * @throws ClassCastException if the class of the specified element prevents it from
	 *     being added to this list
	 * @throws NullPointerException if the specified element is null and this list does
	 *     not permit null elements
	 * @throws IllegalArgumentException if some property of this element prevents it from
	 *     being added to this list
	 */
	@Override
	public final boolean add(ConstraintViolation constraintViolation) {
		return this.delegate.add(constraintViolation);
	}

	/**
	 * Inserts the specified element at the specified position in this list (optional
	 * operation). Shifts the element currently at that position (if any) and any
	 * subsequent elements to the right (adds one to their indices).
	 *
	 * @param index index at which the specified element is to be inserted
	 * @param element element to be inserted
	 * @throws UnsupportedOperationException if the <tt>add</tt> operation is not
	 *     supported by this list
	 * @throws ClassCastException if the class of the specified element prevents it from
	 *     being added to this list
	 * @throws NullPointerException if the specified element is null and this list does
	 *     not permit null elements
	 * @throws IllegalArgumentException if some property of the specified element prevents
	 *     it from being added to this list
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *     (<tt>index &lt; 0 || index &gt; size()</tt>)
	 */
	@Override
	public final void add(int index, ConstraintViolation element) {
		this.delegate.add(index, element);
	}

	/**
	 * Appends all of the elements in the specified collection to the end of this list, in
	 * the order that they are returned by the specified collection's iterator (optional
	 * operation). The behavior of this operation is undefined if the specified collection
	 * is modified while the operation is in progress. (Note that this will occur if the
	 * specified collection is this list, and it's nonempty.)
	 *
	 * @param c collection containing elements to be added to this list
	 * @return <tt>true</tt> if this list changed as a result of the call
	 * @throws UnsupportedOperationException if the <tt>addAll</tt> operation is not
	 *     supported by this list
	 * @throws ClassCastException if the class of an element of the specified collection
	 *     prevents it from being added to this list
	 * @throws NullPointerException if the specified collection contains one or more null
	 *     elements and this list does not permit null elements, or if the specified
	 *     collection is null
	 * @throws IllegalArgumentException if some property of an element of the specified
	 *     collection prevents it from being added to this list
	 * @see #add(Object)
	 */
	@Override
	public final boolean addAll(Collection<? extends ConstraintViolation> c) {
		return this.delegate.addAll(c);
	}

	/**
	 * Inserts all of the elements in the specified collection into this list at the
	 * specified position (optional operation). Shifts the element currently at that
	 * position (if any) and any subsequent elements to the right (increases their
	 * indices). The new elements will appear in this list in the order that they are
	 * returned by the specified collection's iterator. The behavior of this operation is
	 * undefined if the specified collection is modified while the operation is in
	 * progress. (Note that this will occur if the specified collection is this list, and
	 * it's nonempty.)
	 *
	 * @param index index at which to insert the first element from the specified
	 *     collection
	 * @param c collection containing elements to be added to this list
	 * @return <tt>true</tt> if this list changed as a result of the call
	 * @throws UnsupportedOperationException if the <tt>addAll</tt> operation is not
	 *     supported by this list
	 * @throws ClassCastException if the class of an element of the specified collection
	 *     prevents it from being added to this list
	 * @throws NullPointerException if the specified collection contains one or more null
	 *     elements and this list does not permit null elements, or if the specified
	 *     collection is null
	 * @throws IllegalArgumentException if some property of an element of the specified
	 *     collection prevents it from being added to this list
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *     (<tt>index &lt; 0 || index &gt; size()</tt>)
	 */
	@Override
	public final boolean addAll(int index, Collection<? extends ConstraintViolation> c) {
		return this.delegate.addAll(c);
	}

	/**
	 * This method is intended to be used with Spring MVC
	 *
	 * <h3>sample</h3>
	 *
	 * <pre>
	 * <code>&#64;PostMapping("users")
	 * public String createUser(Model model, UserForm userForm, BindingResult bindingResult) {
	 *     return validator.validateToEither(userForm)
	 *         .fold(violations -> {
	 *             violations.apply(bindingResult::rejectValue);
	 *             return "userForm";
	 *         }, form -> {
	 *             // ...
	 *             return "redirect:/";
	 *         });
	 * }</code>
	 * </pre>
	 *
	 * @param callback
	 */
	public ConstraintViolations apply(Callback callback) {
		this.forEach(
				v -> callback.apply(v.name(), v.messageKey(), v.args(), v.message()));
		return this;
	}

	/**
	 * Removes all of the elements from this list (optional operation). The list will be
	 * empty after this call returns.
	 *
	 * @throws UnsupportedOperationException if the <tt>clear</tt> operation is not
	 *     supported by this list
	 */
	@Override
	public final void clear() {
		this.delegate.clear();
	}

	/**
	 * Returns <tt>true</tt> if this list contains the specified element. More formally,
	 * returns <tt>true</tt> if and only if this list contains at least one element
	 * <tt>e</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
	 *
	 * @param o element whose presence in this list is to be tested
	 * @return <tt>true</tt> if this list contains the specified element
	 * @throws ClassCastException if the type of the specified element is incompatible
	 *     with this list (<a href="Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException if the specified element is null and this list does
	 *     not permit null elements
	 *     (<a href="Collection.html#optional-restrictions">optional</a>)
	 */
	@Override
	public final boolean contains(Object o) {
		return this.delegate.contains(o);
	}

	/**
	 * Returns <tt>true</tt> if this list contains all of the elements of the specified
	 * collection.
	 *
	 * @param c collection to be checked for containment in this list
	 * @return <tt>true</tt> if this list contains all of the elements of the specified
	 * collection
	 * @throws ClassCastException if the types of one or more elements in the specified
	 *     collection are incompatible with this list
	 *     (<a href="Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException if the specified collection contains one or more null
	 *     elements and this list does not permit null elements
	 *     (<a href="Collection.html#optional-restrictions">optional</a>), or if the
	 *     specified collection is null
	 * @see #contains(Object)
	 */
	@Override
	public final boolean containsAll(Collection<?> c) {
		return this.delegate.containsAll(c);
	}

	public List<ViolationDetail> details() {
		return this.delegate.stream().map(ConstraintViolation::detail)
				.collect(Collectors.toList());
	}

	/**
	 * Returns the element at the specified position in this list.
	 *
	 * @param index index of the element to return
	 * @return the element at the specified position in this list
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *     (<tt>index &lt; 0 || index &gt;= size()</tt>)
	 */
	@Override
	public final ConstraintViolation get(int index) {
		return this.delegate.get(index);
	}

	/**
	 * Returns the index of the first occurrence of the specified element in this list, or
	 * -1 if this list does not contain the element. More formally, returns the lowest
	 * index <tt>i</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>, or -1 if
	 * there is no such index.
	 *
	 * @param o element to search for
	 * @return the index of the first occurrence of the specified element in this list, or
	 * -1 if this list does not contain the element
	 * @throws ClassCastException if the type of the specified element is incompatible
	 *     with this list (<a href="Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException if the specified element is null and this list does
	 *     not permit null elements
	 *     (<a href="Collection.html#optional-restrictions">optional</a>)
	 */
	@Override
	public final int indexOf(Object o) {
		return this.delegate.indexOf(o);
	}

	/**
	 * Returns <tt>true</tt> if this list contains no elements.
	 *
	 * @return <tt>true</tt> if this list contains no elements
	 */
	@Override
	public final boolean isEmpty() {
		return this.delegate.isEmpty();
	}

	public final boolean isValid() {
		return this.delegate.isEmpty();
	}

	/**
	 * Returns an iterator over the elements in this list in proper sequence.
	 *
	 * @return an iterator over the elements in this list in proper sequence
	 */
	@Override
	public final Iterator<ConstraintViolation> iterator() {
		return this.delegate.iterator();
	}

	/**
	 * Returns the index of the last occurrence of the specified element in this list, or
	 * -1 if this list does not contain the element. More formally, returns the highest
	 * index <tt>i</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>, or -1 if
	 * there is no such index.
	 *
	 * @param o element to search for
	 * @return the index of the last occurrence of the specified element in this list, or
	 * -1 if this list does not contain the element
	 * @throws ClassCastException if the type of the specified element is incompatible
	 *     with this list (<a href="Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException if the specified element is null and this list does
	 *     not permit null elements
	 *     (<a href="Collection.html#optional-restrictions">optional</a>)
	 */
	@Override
	public final int lastIndexOf(Object o) {
		return this.delegate.lastIndexOf(o);
	}

	/**
	 * Returns a list iterator over the elements in this list (in proper sequence).
	 *
	 * @return a list iterator over the elements in this list (in proper sequence)
	 */
	@Override
	public final ListIterator<ConstraintViolation> listIterator() {
		return this.delegate.listIterator();
	}

	/**
	 * Returns a list iterator over the elements in this list (in proper sequence),
	 * starting at the specified position in the list. The specified index indicates the
	 * first element that would be returned by an initial call to {@link ListIterator#next
	 * next}. An initial call to {@link ListIterator#previous previous} would return the
	 * element with the specified index minus one.
	 *
	 * @param index index of the first element to be returned from the list iterator (by a
	 *     call to {@link ListIterator#next next})
	 * @return a list iterator over the elements in this list (in proper sequence),
	 * starting at the specified position in the list
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *     ({@code index < 0 || index > size()})
	 */
	@Override
	public final ListIterator<ConstraintViolation> listIterator(int index) {
		return this.delegate.listIterator(index);
	}

	/**
	 * Removes the first occurrence of the specified element from this list, if it is
	 * present (optional operation). If this list does not contain the element, it is
	 * unchanged. More formally, removes the element with the lowest index <tt>i</tt> such
	 * that <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt> (if
	 * such an element exists). Returns <tt>true</tt> if this list contained the specified
	 * element (or equivalently, if this list changed as a result of the call).
	 *
	 * @param o element to be removed from this list, if present
	 * @return <tt>true</tt> if this list contained the specified element
	 * @throws ClassCastException if the type of the specified element is incompatible
	 *     with this list (<a href="Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException if the specified element is null and this list does
	 *     not permit null elements
	 *     (<a href="Collection.html#optional-restrictions">optional</a>)
	 * @throws UnsupportedOperationException if the <tt>remove</tt> operation is not
	 *     supported by this list
	 */
	@Override
	public final boolean remove(Object o) {
		return this.delegate.remove(o);
	}

	/**
	 * Removes the element at the specified position in this list (optional operation).
	 * Shifts any subsequent elements to the left (subtracts one from their indices).
	 * Returns the element that was removed from the list.
	 *
	 * @param index the index of the element to be removed
	 * @return the element previously at the specified position
	 * @throws UnsupportedOperationException if the <tt>remove</tt> operation is not
	 *     supported by this list
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *     (<tt>index &lt; 0 || index &gt;= size()</tt>)
	 */
	@Override
	public final ConstraintViolation remove(int index) {
		return this.delegate.remove(index);
	}

	/**
	 * Removes from this list all of its elements that are contained in the specified
	 * collection (optional operation).
	 *
	 * @param c collection containing elements to be removed from this list
	 * @return <tt>true</tt> if this list changed as a result of the call
	 * @throws UnsupportedOperationException if the <tt>removeAll</tt> operation is not
	 *     supported by this list
	 * @throws ClassCastException if the class of an element of this list is incompatible
	 *     with the specified collection
	 *     (<a href="Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException if this list contains a null element and the specified
	 *     collection does not permit null elements
	 *     (<a href="Collection.html#optional-restrictions">optional</a>), or if the
	 *     specified collection is null
	 * @see #remove(Object)
	 * @see #contains(Object)
	 */
	@Override
	public final boolean removeAll(Collection<?> c) {
		return this.delegate.removeAll(c);
	}

	/**
	 * Retains only the elements in this list that are contained in the specified
	 * collection (optional operation). In other words, removes from this list all of its
	 * elements that are not contained in the specified collection.
	 *
	 * @param c collection containing elements to be retained in this list
	 * @return <tt>true</tt> if this list changed as a result of the call
	 * @throws UnsupportedOperationException if the <tt>retainAll</tt> operation is not
	 *     supported by this list
	 * @throws ClassCastException if the class of an element of this list is incompatible
	 *     with the specified collection
	 *     (<a href="Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException if this list contains a null element and the specified
	 *     collection does not permit null elements
	 *     (<a href="Collection.html#optional-restrictions">optional</a>), or if the
	 *     specified collection is null
	 * @see #remove(Object)
	 * @see #contains(Object)
	 */
	@Override
	public final boolean retainAll(Collection<?> c) {
		return this.delegate.retainAll(c);
	}

	/**
	 * Replaces the element at the specified position in this list with the specified
	 * element (optional operation).
	 *
	 * @param index index of the element to replace
	 * @param element element to be stored at the specified position
	 * @return the element previously at the specified position
	 * @throws UnsupportedOperationException if the <tt>set</tt> operation is not
	 *     supported by this list
	 * @throws ClassCastException if the class of the specified element prevents it from
	 *     being added to this list
	 * @throws NullPointerException if the specified element is null and this list does
	 *     not permit null elements
	 * @throws IllegalArgumentException if some property of the specified element prevents
	 *     it from being added to this list
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *     (<tt>index &lt; 0 || index &gt;= size()</tt>)
	 */
	@Override
	public final ConstraintViolation set(int index, ConstraintViolation element) {
		return this.delegate.set(index, element);
	}

	/**
	 * Returns the number of elements in this list. If this list contains more than
	 * <tt>Integer.MAX_VALUE</tt> elements, returns <tt>Integer.MAX_VALUE</tt>.
	 *
	 * @return the number of elements in this list
	 */
	@Override
	public final int size() {
		return this.delegate.size();
	}

	/**
	 * Returns a view of the portion of this list between the specified
	 * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive. (If
	 * <tt>fromIndex</tt> and <tt>toIndex</tt> are equal, the returned list is empty.) The
	 * returned list is backed by this list, so non-structural changes in the returned
	 * list are reflected in this list, and vice-versa. The returned list supports all of
	 * the optional list operations supported by this list.
	 * <p>
	 * <p>
	 * This method eliminates the need for explicit range operations (of the sort that
	 * commonly exist for arrays). Any operation that expects a list can be used as a
	 * range operation by passing a subList view instead of a whole list. For example, the
	 * following idiom removes a range of elements from a list:
	 *
	 * <pre>
	 * {@code
	 *      list.subList(from, to).clear();
	 * }
	 * </pre>
	 *
	 * Similar idioms may be constructed for <tt>indexOf</tt> and <tt>lastIndexOf</tt>,
	 * and all of the algorithms in the <tt>Collections</tt> class can be applied to a
	 * subList.
	 * <p>
	 * <p>
	 * The semantics of the list returned by this method become undefined if the backing
	 * list (i.e., this list) is <i>structurally modified</i> in any way other than via
	 * the returned list. (Structural modifications are those that change the size of this
	 * list, or otherwise perturb it in such a fashion that iterations in progress may
	 * yield incorrect results.)
	 *
	 * @param fromIndex low endpoint (inclusive) of the subList
	 * @param toIndex high endpoint (exclusive) of the subList
	 * @return a view of the specified range within this list
	 * @throws IndexOutOfBoundsException for an illegal endpoint index value
	 *     (<tt>fromIndex &lt; 0 || toIndex &gt; size ||
	 *                                   fromIndex &gt; toIndex</tt>)
	 */
	@Override
	public final List<ConstraintViolation> subList(int fromIndex, int toIndex) {
		return this.delegate.subList(fromIndex, toIndex);
	}

	public final <E extends RuntimeException> void throwIfInvalid(
			Function<ConstraintViolations, E> toException) throws E {
		if (!isValid()) {
			throw toException.apply(this);
		}
	}

	/**
	 * Returns an array containing all of the elements in this list in proper sequence
	 * (from first to last element).
	 *
	 * <p>
	 * The returned array will be "safe" in that no references to it are maintained by
	 * this list. (In other words, this method must allocate a new array even if this list
	 * is backed by an array). The caller is thus free to modify the returned array.
	 *
	 * <p>
	 * This method acts as bridge between array-based and collection-based APIs.
	 *
	 * @return an array containing all of the elements in this list in proper sequence
	 * @see Arrays#asList(Object[])
	 */
	@Override
	public final Object[] toArray() {
		return this.delegate.toArray();
	}

	/**
	 * Returns an array containing all of the elements in this list in proper sequence
	 * (from first to last element); the runtime type of the returned array is that of the
	 * specified array. If the list fits in the specified array, it is returned therein.
	 * Otherwise, a new array is allocated with the runtime type of the specified array
	 * and the size of this list.
	 *
	 * <p>
	 * If the list fits in the specified array with room to spare (i.e., the array has
	 * more elements than the list), the element in the array immediately following the
	 * end of the list is set to <tt>null</tt>. (This is useful in determining the length
	 * of the list <i>only</i> if the caller knows that the list does not contain any null
	 * elements.)
	 *
	 * <p>
	 * Like the {@link #toArray()} method, this method acts as bridge between array-based
	 * and collection-based APIs. Further, this method allows precise control over the
	 * runtime type of the output array, and may, under certain circumstances, be used to
	 * save allocation costs.
	 *
	 * <p>
	 * Suppose <tt>x</tt> is a list known to contain only strings. The following code can
	 * be used to dump the list into a newly allocated array of <tt>String</tt>:
	 *
	 * <pre>
	 * {
	 * 	&#64;code
	 * 	String[] y = x.toArray(new String[0]);
	 * }
	 * </pre>
	 * <p>
	 * Note that <tt>toArray(new Object[0])</tt> is identical in function to
	 * <tt>toArray()</tt>.
	 *
	 * @param a the array into which the elements of this list are to be stored, if it is
	 *     big enough; otherwise, a new array of the same runtime type is allocated for
	 *     this purpose.
	 * @return an array containing the elements of this list
	 * @throws ArrayStoreException if the runtime type of the specified array is not a
	 *     supertype of the runtime type of every element in this list
	 * @throws NullPointerException if the specified array is null
	 */
	@Override
	public final <T> T[] toArray(T[] a) {
		return this.delegate.toArray(a);
	}

	@Override
	public String toString() {
		return this.delegate.toString();
	}

	public final List<ConstraintViolation> violations() {
		return Collections.unmodifiableList(this.delegate);
	}

	@FunctionalInterface
	public interface Callback {
		void apply(String name, String messageKey, Object[] args, String defaultMessage);
	}
}
