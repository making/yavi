package am.ik.yavi.constraint.charsequence;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static am.ik.yavi.constraint.ViolationMessage.Default.CODE_POINTS_ALL_INCLUDED;
import static am.ik.yavi.constraint.ViolationMessage.Default.CODE_POINTS_NOT_INCLUDED;
import static am.ik.yavi.core.NullValidity.NULL_IS_VALID;

import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.core.ConstraintPredicate;
import am.ik.yavi.core.ViolatedValue;

public class CodePointsConstraint<T, E extends CharSequence>
		extends CharSequenceConstraint<T, E> {
	private final CodePoints<E> codePoints;

	public CodePointsConstraint(CharSequenceConstraint<T, E> delegate,
			CodePoints<E> codePoints) {
		super();
		this.codePoints = codePoints;
		this.predicates().addAll(delegate.predicates());
	}

	@Override
	public CodePointsConstraint<T, E> cast() {
		return this;
	}

	public CodePointsConstraint<T, E> allIncluded() {
		this.predicates().add(ConstraintPredicate.withViolatedValue(x -> {
			Set<Integer> excludedFromWhiteList = this.codePoints.allExcludedCodePoints(x);
			if (excludedFromWhiteList.isEmpty()) {
				return Optional.empty();
			}
			List<String> excludedList = excludedFromWhiteList.stream() //
					.map(i -> new String(new int[] { i }, 0, 1)) //
					.collect(Collectors.toList());
			return Optional.of(new ViolatedValue(excludedList));
		}, CODE_POINTS_ALL_INCLUDED, () -> new Object[] {}, NULL_IS_VALID));
		return this;
	}

	public CodePointsConstraint<T, E> notIncluded() {
		this.predicates().add(ConstraintPredicate.withViolatedValue(x -> {
			Set<Integer> excludedFromBlackList = this.codePoints.allExcludedCodePoints(x);
			Integer codePoint;
			String str = x.toString();
			int len = str.length();
			Set<Integer> included = new LinkedHashSet<>();
			for (int i = 0; i < len; i += Character.charCount(codePoint)) {
				codePoint = str.codePointAt(i);
				if (!excludedFromBlackList.contains(codePoint)) {
					included.add(codePoint);
				}
			}
			if (included.isEmpty()) {
				return Optional.empty();
			}
			List<String> includedList = included.stream() //
					.map(i -> new String(new int[] { i }, 0, 1)) //
					.collect(Collectors.toList());
			return Optional.of(new ViolatedValue(includedList));
		}, CODE_POINTS_NOT_INCLUDED, () -> new Object[] {}, NULL_IS_VALID));
		return this;
	}

}
