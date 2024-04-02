package am.ik.yavi.arguments;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import am.ik.yavi.builder.IntegerValidatorBuilder;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validated;
import am.ik.yavi.jsr305.Nullable;
import am.ik.yavi.validator.Yavi;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Gh286Test {
	final Arguments1Validator<String, Integer> limitValidatgor = StringValidatorBuilder
			.of("limit", c -> c.isInteger())
			.build(s -> s != null && !s.isEmpty() ? Integer.parseInt(s) : 1_000)
			.andThen(IntegerValidatorBuilder
					.of("limit", c -> c.notNull().positive().lessThanOrEqual(1_000))
					.build());

	final Arguments1Validator<Map<String, String>, PaginationParam> validator = Yavi
			.arguments() //
			._string("cursor", c -> c.notBlank()) //
			._integer(limitValidatgor) //
			.apply(PaginationParam::new) //
			.compose(queryParams -> Arguments.of(queryParams.get("cursor"),
					queryParams.get("limit")));

	@Test
	void valid() {
		PaginationParam param = validator.validated(new HashMap<String, String>() {
			{
				put("limit", "200");
				put("cursor", "test");
			}
		});
		assertThat(param).isEqualTo(new PaginationParam("test", 200));
	}

	@Test
	void valid_default_limit() {
		PaginationParam param = validator.validated(new HashMap<String, String>() {
			{
				put("cursor", "test");
			}
		});
		assertThat(param).isEqualTo(new PaginationParam("test", 1_000));
	}

	@Test
	void invalid_empty_map() {
		Validated<PaginationParam> validated = validator.validate(Collections.emptyMap());
		assertThat(validated.isValid()).isFalse();
		ConstraintViolations violations = validated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("cursor");
		assertThat(violations.get(0).messageKey()).isEqualTo("charSequence.notBlank");
	}

	@Test
	void invalid_limit_negative() {
		Validated<PaginationParam> validated = validator
				.validate(new HashMap<String, String>() {
					{
						put("cursor", "  ");
						put("limit", "-1");
					}
				});
		assertThat(validated.isValid()).isFalse();
		ConstraintViolations violations = validated.errors();
		assertThat(violations).hasSize(2);
		assertThat(violations.get(0).name()).isEqualTo("cursor");
		assertThat(violations.get(0).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(1).name()).isEqualTo("limit");
		assertThat(violations.get(1).messageKey()).isEqualTo("numeric.positive");
	}

	@Test
	void invalid_limit_over_limit() {
		Validated<PaginationParam> validated = validator
				.validate(new HashMap<String, String>() {
					{
						put("cursor", "  ");
						put("limit", "1001");
					}
				});
		assertThat(validated.isValid()).isFalse();
		ConstraintViolations violations = validated.errors();
		assertThat(violations).hasSize(2);
		assertThat(violations.get(0).name()).isEqualTo("cursor");
		assertThat(violations.get(0).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(1).name()).isEqualTo("limit");
		assertThat(violations.get(1).messageKey()).isEqualTo("numeric.lessThanOrEqual");
	}

	@Test
	void invalid_limit_not_integer() {
		Validated<PaginationParam> validated = validator
				.validate(new HashMap<String, String>() {
					{
						put("cursor", "  ");
						put("limit", "abc");
					}
				});
		assertThat(validated.isValid()).isFalse();
		ConstraintViolations violations = validated.errors();
		assertThat(violations).hasSize(2);
		assertThat(violations.get(0).name()).isEqualTo("cursor");
		assertThat(violations.get(0).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(1).name()).isEqualTo("limit");
		assertThat(violations.get(1).messageKey()).isEqualTo("charSequence.integer");
	}

	public static class PaginationParam {
		@Nullable
		private final String cursor;

		private final int maxPageSize;

		public PaginationParam(@Nullable String cursor, int maxPageSize) {
			this.cursor = cursor;
			this.maxPageSize = maxPageSize;
		}

		@Nullable
		public String cursor() {
			return cursor;
		}

		public int maxPageSize() {
			return maxPageSize;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof PaginationParam))
				return false;
			PaginationParam that = (PaginationParam) o;
			return maxPageSize == that.maxPageSize && Objects.equals(cursor, that.cursor);
		}

		@Override
		public int hashCode() {
			return Objects.hash(cursor, maxPageSize);
		}
	}
}
