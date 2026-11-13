/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Function;

import am.ik.yavi.message.MessageFormatter;
import org.jspecify.annotations.Nullable;

/**
 * A mutable {@link List} of {@link ConstraintViolation} instances.
 *
 * <p>
 * Instances are returned when validation fails and are empty when validation succeeds;
 * use {@link #isValid()} to check the outcome. The list is not thread-safe.
 *
 * @since 0.0.1
 */
public class ConstraintViolations implements List<ConstraintViolation> {

	private final List<ConstraintViolation> delegate;

	/**
	 * Returns the given violations as a {@code ConstraintViolations} instance. If the
	 * argument already is a {@code ConstraintViolations}, it is returned as-is.
	 * @param delegate the violations to wrap
	 * @return the given violations, or a new instance wrapping them
	 * @since 0.6.0
	 */
	public static ConstraintViolations of(List<ConstraintViolation> delegate) {
		if (delegate instanceof ConstraintViolations) {
			return (ConstraintViolations) delegate;
		}
		return new ConstraintViolations(delegate);
	}

	/**
	 * Constructs an empty list with an initial capacity of ten.
	 */
	public ConstraintViolations() {
		this.delegate = new ArrayList<>();
	}

	/**
	 * Constructs an instance that delegates to the given list. The list is not copied, so
	 * subsequent changes to it are reflected by this instance.
	 * @param delegate the violations to delegate to
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
	 * @param constraintViolation element to be appended to this list
	 * @return <code>true</code> (as specified by {@link Collection#add})
	 * @throws UnsupportedOperationException if the <code>add</code> operation is not
	 * supported by this list
	 * @throws ClassCastException if the class of the specified element prevents it from
	 * being added to this list
	 * @throws NullPointerException if the specified element is null and this list does
	 * not permit null elements
	 * @throws IllegalArgumentException if some property of this element prevents it from
	 * being added to this list
	 */
	@Override
	public final boolean add(ConstraintViolation constraintViolation) {
		return this.delegate.add(constraintViolation);
	}

	/**
	 * Inserts the specified element at the specified position in this list (optional
	 * operation). Shifts the element currently at that position (if any) and any
	 * subsequent elements to the right (adds one to their indices).
	 * @param index index at which the specified element is to be inserted
	 * @param element element to be inserted
	 * @throws UnsupportedOperationException if the <code>add</code> operation is not
	 * supported by this list
	 * @throws ClassCastException if the class of the specified element prevents it from
	 * being added to this list
	 * @throws NullPointerException if the specified element is null and this list does
	 * not permit null elements
	 * @throws IllegalArgumentException if some property of the specified element prevents
	 * it from being added to this list
	 * @throws IndexOutOfBoundsException if the index is out of range
	 * (<code>index &lt; 0 || index &gt; size()</code>)
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
	 * @param c collection containing elements to be added to this list
	 * @return <code>true</code> if this list changed as a result of the call
	 * @throws UnsupportedOperationException if the <code>addAll</code> operation is not
	 * supported by this list
	 * @throws ClassCastException if the class of an element of the specified collection
	 * prevents it from being added to this list
	 * @throws NullPointerException if the specified collection contains one or more null
	 * elements and this list does not permit null elements, or if the specified
	 * collection is null
	 * @throws IllegalArgumentException if some property of an element of the specified
	 * collection prevents it from being added to this list
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
	 * indices).
	 *
	 * <p>
	 * Note that this implementation delegates to {@link #addAll(Collection)} and ignores
	 * {@code index}; the elements are appended to the end of this list.
	 * @param index index at which to insert the first element from the specified
	 * collection
	 * @param c collection containing elements to be added to this list
	 * @return <code>true</code> if this list changed as a result of the call
	 * @throws UnsupportedOperationException if the <code>addAll</code> operation is not
	 * supported by this list
	 * @throws ClassCastException if the class of an element of the specified collection
	 * prevents it from being added to this list
	 * @throws NullPointerException if the specified collection contains one or more null
	 * elements and this list does not permit null elements, or if the specified
	 * collection is null
	 * @throws IllegalArgumentException if some property of an element of the specified
	 * collection prevents it from being added to this list
	 */
	@Override
	public final boolean addAll(int index, Collection<? extends ConstraintViolation> c) {
		return this.delegate.addAll(c);
	}

	/**
	 * Passes every violation to the given callback and returns this list.
	 *
	 * <p>
	 * This method is intended to be used with Spring MVC:
	 *
	 * <pre>{@code
	 * ConstraintViolations violations = validator.validate(userForm);
	 * violations.apply(bindingResult::rejectValue);
	 * }</pre>
	 * @param callback the callback invoked for each violation
	 * @return this list
	 * @since 0.0.7
	 */
	public ConstraintViolations apply(Callback callback) {
		this.forEach(v -> callback.apply(v.name(), v.messageKey(), v.args(), v.message()));
		return this;
	}

	/**
	 * Removes all of the elements from this list (optional operation). The list will be
	 * empty after this call returns.
	 * @throws UnsupportedOperationException if the <code>clear</code> operation is not
	 * supported by this list
	 */
	@Override
	public final void clear() {
		this.delegate.clear();
	}

	/**
	 * Returns <code>true</code> if this list contains the specified element. More
	 * formally, returns <code>true</code> if and only if this list contains at least one
	 * element <code>e</code> such that
	 * <code>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</code>.
	 * @param o element whose presence in this list is to be tested
	 * @return <code>true</code> if this list contains the specified element
	 * @throws ClassCastException if the type of the specified element is incompatible
	 * with this list (optional)
	 * @throws NullPointerException if the specified element is null and this list does
	 * not permit null elements (optional)
	 */
	@Override
	public final boolean contains(Object o) {
		return this.delegate.contains(o);
	}

	/**
	 * Returns <code>true</code> if this list contains all of the elements of the
	 * specified collection.
	 * @param c collection to be checked for containment in this list
	 * @return <code>true</code> if this list contains all of the elements of the
	 * specified collection
	 * @throws ClassCastException if the types of one or more elements in the specified
	 * collection are incompatible with this list (optional)
	 * @throws NullPointerException if the specified collection contains one or more null
	 * elements and this list does not permit null elements (optional), or if the
	 * specified collection is null
	 * @see #contains(Object)
	 */
	@Override
	public final boolean containsAll(Collection<?> c) {
		return this.delegate.containsAll(c);
	}

	/**
	 * Returns the violations in a form that is ready to be serialized, for example as the
	 * body of an API response.
	 *
	 * <p>
	 * Every {@link ViolationDetail} tells which value failed, with the name and the JSON
	 * Pointer, and why, with the message id, the formatted message and the arguments:
	 *
	 * <pre>{@code
	 * [{"name": "items[1].name", "pointer": "/items/1/name", "messageKey": "charSequence.notBlank",
	 *   "message": "\"items[1].name\" must not be blank", "args": ["items[1].name", ""]}]
	 * }</pre>
	 * @return an unmodifiable list of violation details, one per violation in the order
	 * of the violations
	 * @since 0.0.3
	 * @see ConstraintViolation#detail()
	 */
	public List<ViolationDetail> details() {
		return this.delegate.stream().map(ConstraintViolation::detail).toList();
	}

	/**
	 * Returns the element at the specified position in this list.
	 * @param index index of the element to return
	 * @return the element at the specified position in this list
	 * @throws IndexOutOfBoundsException if the index is out of range
	 * (<code>index &lt; 0 || index &gt;= size()</code>)
	 */
	@Override
	public final ConstraintViolation get(int index) {
		return this.delegate.get(index);
	}

	/**
	 * Returns the index of the first occurrence of the specified element in this list, or
	 * -1 if this list does not contain the element. More formally, returns the lowest
	 * index <code>i</code> such that
	 * <code>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</code>, or -1
	 * if there is no such index.
	 * @param o element to search for
	 * @return the index of the first occurrence of the specified element in this list, or
	 * -1 if this list does not contain the element
	 * @throws ClassCastException if the type of the specified element is incompatible
	 * with this list (optional)
	 * @throws NullPointerException if the specified element is null and this list does
	 * not permit null elements (optional)
	 */
	@Override
	public final int indexOf(Object o) {
		return this.delegate.indexOf(o);
	}

	/**
	 * Returns <code>true</code> if this list contains no elements.
	 * @return <code>true</code> if this list contains no elements
	 */
	@Override
	public final boolean isEmpty() {
		return this.delegate.isEmpty();
	}

	/**
	 * Returns whether validation succeeded, that is whether this list contains no
	 * violations.
	 * @return {@code true} if this list is empty, {@code false} otherwise
	 * @since 0.0.1
	 */
	public final boolean isValid() {
		return this.delegate.isEmpty();
	}

	/**
	 * Returns an iterator over the elements in this list in proper sequence.
	 * @return an iterator over the elements in this list in proper sequence
	 */
	@Override
	public final Iterator<ConstraintViolation> iterator() {
		return this.delegate.iterator();
	}

	/**
	 * Returns the messages of the violations grouped by the
	 * {@linkplain ConstraintViolation#name() name} of the value that failed, the form
	 * that shows the messages next to the fields of a form.
	 *
	 * <p>
	 * The names are in the order in which they first appear in the violations, and so are
	 * the messages of a name:
	 *
	 * <pre>{@code
	 * {"email": ["\"email\" must not be blank"],
	 *  "items[1].name": ["\"items[1].name\" must not be blank"]}
	 * }</pre>
	 *
	 * The messages of the violations on the validated value itself are under the empty
	 * name.
	 * @return an unmodifiable map of the name to the messages
	 * @since 0.18.0
	 */
	public Map<String, List<String>> messagesByName() {
		return this.messagesBy(ConstraintViolation::name);
	}

	/**
	 * Returns the messages of the violations grouped by the
	 * {@linkplain ConstraintViolation#pointer() JSON Pointer} to the value that failed.
	 *
	 * <p>
	 * This is {@link #messagesByName()} with the keys in the form that a client of a JSON
	 * API resolves against the document that it sent: {@code /items/1/name} instead of
	 * {@code items[1].name}. The messages of the violations on the validated value itself
	 * are under the empty pointer.
	 * @return an unmodifiable map of the JSON Pointer to the messages
	 * @since 0.18.0
	 */
	public Map<String, List<String>> messagesByPointer() {
		return this.messagesBy(ConstraintViolation::pointer);
	}

	/**
	 * Returns the violations with the labels that the given function gives them, so that
	 * a message calls the value {@code Email address} instead of {@code email} (see
	 * {@link ConstraintViolation#labeled(String)}). The names and the paths are
	 * unchanged, so {@link #messagesByName()} still has the name of the field as the key:
	 *
	 * <pre>{@code
	 * Map<String, String> labels = Map.of("email", "Email address");
	 * violations.labeled(violation -> labels.get(violation.name())).messagesByName();
	 * // {email=["Email address" must not be blank]}
	 * }</pre>
	 *
	 * The function is the place to look a label up in a resource bundle, and to decide
	 * what a nested value such as {@code items[1].name} is called from its
	 * {@link ConstraintViolation#path() path}.
	 * @param label the function that returns the label of a violation, or {@code null} to
	 * leave the violation as it is
	 * @return new violations with the labels
	 * @since 0.18.0
	 */
	public ConstraintViolations labeled(Function<? super ConstraintViolation, ? extends @Nullable String> label) {
		final List<ConstraintViolation> labeled = new ArrayList<>(this.delegate.size());
		for (ConstraintViolation violation : this.delegate) {
			final String given = label.apply(violation);
			labeled.add(given == null ? violation : violation.labeled(given));
		}
		return new ConstraintViolations(labeled);
	}

	/**
	 * Returns the violations with the given message formatter instead of the one of the
	 * validator that reported them, so that {@link #messagesByName()},
	 * {@link #messagesByPointer()}, {@link #details()} and {@link #apply(Callback)}
	 * format the messages with it:
	 *
	 * <pre>{@code
	 * violations.formattedWith(new MessageSourceMessageFormatter(messageSource::getMessage))
	 *     .messagesByName();
	 * }</pre>
	 *
	 * A validator does this to every violation it reports with
	 * {@link ValueValidator#formattingWith(MessageFormatter)}.
	 * @param messageFormatter the message formatter of the returned violations
	 * @return new violations with the message formatter
	 * @since 0.18.0
	 * @see ConstraintViolation#formattedWith(MessageFormatter)
	 */
	public ConstraintViolations formattedWith(MessageFormatter messageFormatter) {
		final List<ConstraintViolation> formatted = new ArrayList<>(this.delegate.size());
		for (ConstraintViolation violation : this.delegate) {
			formatted.add(violation.formattedWith(messageFormatter));
		}
		return new ConstraintViolations(formatted);
	}

	private Map<String, List<String>> messagesBy(Function<ConstraintViolation, String> key) {
		final Map<String, List<String>> messages = new LinkedHashMap<>();
		for (ConstraintViolation violation : this.delegate) {
			messages.computeIfAbsent(key.apply(violation), k -> new ArrayList<>()).add(violation.message());
		}
		messages.replaceAll((k, v) -> Collections.unmodifiableList(v));
		return Collections.unmodifiableMap(messages);
	}

	/**
	 * Returns the index of the last occurrence of the specified element in this list, or
	 * -1 if this list does not contain the element. More formally, returns the highest
	 * index <code>i</code> such that
	 * <code>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</code>, or -1
	 * if there is no such index.
	 * @param o element to search for
	 * @return the index of the last occurrence of the specified element in this list, or
	 * -1 if this list does not contain the element
	 * @throws ClassCastException if the type of the specified element is incompatible
	 * with this list (optional)
	 * @throws NullPointerException if the specified element is null and this list does
	 * not permit null elements (optional)
	 */
	@Override
	public final int lastIndexOf(Object o) {
		return this.delegate.lastIndexOf(o);
	}

	/**
	 * Returns a list iterator over the elements in this list (in proper sequence).
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
	 * @param index index of the first element to be returned from the list iterator (by a
	 * call to {@link ListIterator#next next})
	 * @return a list iterator over the elements in this list (in proper sequence),
	 * starting at the specified position in the list
	 * @throws IndexOutOfBoundsException if the index is out of range
	 * ({@code index < 0 || index > size()})
	 */
	@Override
	public final ListIterator<ConstraintViolation> listIterator(int index) {
		return this.delegate.listIterator(index);
	}

	/**
	 * Removes the first occurrence of the specified element from this list, if it is
	 * present (optional operation). If this list does not contain the element, it is
	 * unchanged. More formally, removes the element with the lowest index <code>i</code>
	 * such that
	 * <code>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</code> (if
	 * such an element exists). Returns <code>true</code> if this list contained the
	 * specified element (or equivalently, if this list changed as a result of the call).
	 * @param o element to be removed from this list, if present
	 * @return <code>true</code> if this list contained the specified element
	 * @throws ClassCastException if the type of the specified element is incompatible
	 * with this list (optional)
	 * @throws NullPointerException if the specified element is null and this list does
	 * not permit null elements (optional)
	 * @throws UnsupportedOperationException if the <code>remove</code> operation is not
	 * supported by this list
	 */
	@Override
	public final boolean remove(Object o) {
		return this.delegate.remove(o);
	}

	/**
	 * Removes the element at the specified position in this list (optional operation).
	 * Shifts any subsequent elements to the left (subtracts one from their indices).
	 * Returns the element that was removed from the list.
	 * @param index the index of the element to be removed
	 * @return the element previously at the specified position
	 * @throws UnsupportedOperationException if the <code>remove</code> operation is not
	 * supported by this list
	 * @throws IndexOutOfBoundsException if the index is out of range
	 * (<code>index &lt; 0 || index &gt;= size()</code>)
	 */
	@Override
	public final ConstraintViolation remove(int index) {
		return this.delegate.remove(index);
	}

	/**
	 * Removes from this list all of its elements that are contained in the specified
	 * collection (optional operation).
	 * @param c collection containing elements to be removed from this list
	 * @return <code>true</code> if this list changed as a result of the call
	 * @throws UnsupportedOperationException if the <code>removeAll</code> operation is
	 * not supported by this list
	 * @throws ClassCastException if the class of an element of this list is incompatible
	 * with the specified collection (optional)
	 * @throws NullPointerException if this list contains a null element and the specified
	 * collection does not permit null elements (optional), or if the specified collection
	 * is null
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
	 * @param c collection containing elements to be retained in this list
	 * @return <code>true</code> if this list changed as a result of the call
	 * @throws UnsupportedOperationException if the <code>retainAll</code> operation is
	 * not supported by this list
	 * @throws ClassCastException if the class of an element of this list is incompatible
	 * with the specified collection (optional)
	 * @throws NullPointerException if this list contains a null element and the specified
	 * collection does not permit null elements (optional), or if the specified collection
	 * is null
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
	 * @param index index of the element to replace
	 * @param element element to be stored at the specified position
	 * @return the element previously at the specified position
	 * @throws UnsupportedOperationException if the <code>set</code> operation is not
	 * supported by this list
	 * @throws ClassCastException if the class of the specified element prevents it from
	 * being added to this list
	 * @throws NullPointerException if the specified element is null and this list does
	 * not permit null elements
	 * @throws IllegalArgumentException if some property of the specified element prevents
	 * it from being added to this list
	 * @throws IndexOutOfBoundsException if the index is out of range
	 * (<code>index &lt; 0 || index &gt;= size()</code>)
	 */
	@Override
	public final ConstraintViolation set(int index, ConstraintViolation element) {
		return this.delegate.set(index, element);
	}

	/**
	 * Returns the number of elements in this list. If this list contains more than
	 * <code>Integer.MAX_VALUE</code> elements, returns <code>Integer.MAX_VALUE</code>.
	 * @return the number of elements in this list
	 */
	@Override
	public final int size() {
		return this.delegate.size();
	}

	/**
	 * Returns a view of the portion of this list between the specified
	 * <code>fromIndex</code>, inclusive, and <code>toIndex</code>, exclusive. (If
	 * <code>fromIndex</code> and <code>toIndex</code> are equal, the returned list is
	 * empty.) The returned list is backed by this list, so non-structural changes in the
	 * returned list are reflected in this list, and vice-versa. The returned list
	 * supports all of the optional list operations supported by this list.
	 *
	 * <p>
	 * This method eliminates the need for explicit range operations (of the sort that
	 * commonly exist for arrays). Any operation that expects a list can be used as a
	 * range operation by passing a subList view instead of a whole list. For example, the
	 * following idiom removes a range of elements from a list:
	 *
	 * <pre>{@code
	 * list.subList(from, to).clear();
	 * }</pre>
	 *
	 * Similar idioms may be constructed for <code>indexOf</code> and
	 * <code>lastIndexOf</code>, and all of the algorithms in the <code>Collections</code>
	 * class can be applied to a subList.
	 *
	 * <p>
	 * The semantics of the list returned by this method become undefined if the backing
	 * list (i.e., this list) is <i>structurally modified</i> in any way other than via
	 * the returned list. (Structural modifications are those that change the size of this
	 * list, or otherwise perturb it in such a fashion that iterations in progress may
	 * yield incorrect results.)
	 * @param fromIndex low endpoint (inclusive) of the subList
	 * @param toIndex high endpoint (exclusive) of the subList
	 * @return a view of the specified range within this list
	 * @throws IndexOutOfBoundsException for an illegal endpoint index value
	 * (<code>fromIndex &lt; 0 || toIndex &gt; size || fromIndex &gt; toIndex</code>)
	 */
	@Override
	public final List<ConstraintViolation> subList(int fromIndex, int toIndex) {
		return this.delegate.subList(fromIndex, toIndex);
	}

	/**
	 * Throws the exception produced by the given function if this list contains at least
	 * one violation.
	 * @param toException the function that creates the exception from these violations
	 * @param <E> the exception type
	 * @throws E if this list is not valid
	 * @since 0.0.3
	 */
	public final <E extends RuntimeException> void throwIfInvalid(Function<ConstraintViolations, E> toException)
			throws E {
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
	 * end of the list is set to <code>null</code>. (This is useful in determining the
	 * length of the list <i>only</i> if the caller knows that the list does not contain
	 * any null elements.)
	 *
	 * <p>
	 * Like the {@link #toArray()} method, this method acts as bridge between array-based
	 * and collection-based APIs. Further, this method allows precise control over the
	 * runtime type of the output array, and may, under certain circumstances, be used to
	 * save allocation costs.
	 *
	 * <p>
	 * Suppose <code>x</code> is a list known to contain only strings. The following code
	 * can be used to dump the list into a newly allocated array of <code>String</code>:
	 *
	 * <pre>{@code
	 * String[] y = x.toArray(new String[0]);
	 * }</pre>
	 *
	 * Note that <code>toArray(new Object[0])</code> is identical in function to
	 * <code>toArray()</code>.
	 * @param a the array into which the elements of this list are to be stored, if it is
	 * big enough; otherwise, a new array of the same runtime type is allocated for this
	 * purpose.
	 * @return an array containing the elements of this list
	 * @throws ArrayStoreException if the runtime type of the specified array is not a
	 * supertype of the runtime type of every element in this list
	 * @throws NullPointerException if the specified array is null
	 */
	@Override
	public final <T> T[] toArray(T[] a) {
		return this.delegate.toArray(a);
	}

	/**
	 * Returns a string representation of the violations.
	 * @return a string representation of the violations
	 */
	@Override
	public String toString() {
		return this.delegate.toString();
	}

	/**
	 * Returns an unmodifiable view of the violations.
	 * @return an unmodifiable view of the violations
	 * @since 0.0.1
	 */
	public final List<ConstraintViolation> violations() {
		return Collections.unmodifiableList(this.delegate);
	}

	/**
	 * Callback invoked for each violation by
	 * {@link ConstraintViolations#apply(Callback)}.
	 *
	 * @since 0.0.7
	 */
	@FunctionalInterface
	public interface Callback {

		/**
		 * Handles a single violation.
		 * @param name the property name that failed validation
		 * @param messageKey the message id of the violated constraint
		 * @param args the message arguments
		 * @param defaultMessage the formatted violation message
		 */
		void apply(String name, String messageKey, Object[] args, String defaultMessage);

	}

}
