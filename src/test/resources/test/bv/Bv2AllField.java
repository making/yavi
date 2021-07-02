package test.bv;

import java.math.BigDecimal;

import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NegativeOrZero;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

public class Bv2AllField {
	@AssertTrue
	private boolean assertTrueValue;

	@AssertFalse
	private boolean assertFalseValue;

	@Null
	private String nullValue;

	@NotNull
	private String notNullValue;

	@NotEmpty
	private String notEmptyValue;

	@NotBlank
	private String notBlankValue;

	@Min(0)
	private Integer minValue;

	@Max(100)
	private Integer maxValue;

	@DecimalMin("2.0")
	private BigDecimal decimalMinValue;

	@DecimalMax("2000.0")
	private BigDecimal decimalMaxValue;

	@Positive
	private Integer positiveValue;

	@PositiveOrZero
	private Integer positiveOrZeroValue;

	@Negative
	private Integer negativeValue;

	@NegativeOrZero
	private Integer negativeOrZeroValue;

	@Size(min = 3, max = 10)
	private String sizeValue;

	@Email
	private String emailValue;

	public boolean isAssertTrueValue() {
		return assertTrueValue;
	}

	public void setAssertTrueValue(boolean assertTrueValue) {
		this.assertTrueValue = assertTrueValue;
	}

	public boolean isAssertFalseValue() {
		return assertFalseValue;
	}

	public void setAssertFalseValue(boolean assertFalseValue) {
		this.assertFalseValue = assertFalseValue;
	}

	public String getNullValue() {
		return nullValue;
	}

	public void setNullValue(String nullValue) {
		this.nullValue = nullValue;
	}

	public String getNotNullValue() {
		return notNullValue;
	}

	public void setNotNullValue(String notNullValue) {
		this.notNullValue = notNullValue;
	}

	public String getNotEmptyValue() {
		return notEmptyValue;
	}

	public void setNotEmptyValue(String notEmptyValue) {
		this.notEmptyValue = notEmptyValue;
	}

	public String getNotBlankValue() {
		return notBlankValue;
	}

	public void setNotBlankValue(String notBlankValue) {
		this.notBlankValue = notBlankValue;
	}

	public Integer getMinValue() {
		return minValue;
	}

	public void setMinValue(Integer minValue) {
		this.minValue = minValue;
	}

	public Integer getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Integer maxValue) {
		this.maxValue = maxValue;
	}

	public BigDecimal getDecimalMinValue() {
		return decimalMinValue;
	}

	public void setDecimalMinValue(BigDecimal decimalMinValue) {
		this.decimalMinValue = decimalMinValue;
	}

	public BigDecimal getDecimalMaxValue() {
		return decimalMaxValue;
	}

	public void setDecimalMaxValue(BigDecimal decimalMaxValue) {
		this.decimalMaxValue = decimalMaxValue;
	}

	public Integer getPositiveValue() {
		return positiveValue;
	}

	public void setPositiveValue(Integer positiveValue) {
		this.positiveValue = positiveValue;
	}

	public Integer getPositiveOrZeroValue() {
		return positiveOrZeroValue;
	}

	public void setPositiveOrZeroValue(Integer positiveOrZeroValue) {
		this.positiveOrZeroValue = positiveOrZeroValue;
	}

	public Integer getNegativeValue() {
		return negativeValue;
	}

	public void setNegativeValue(Integer negativeValue) {
		this.negativeValue = negativeValue;
	}

	public Integer getNegativeOrZeroValue() {
		return negativeOrZeroValue;
	}

	public void setNegativeOrZeroValue(Integer negativeOrZeroValue) {
		this.negativeOrZeroValue = negativeOrZeroValue;
	}

	public String getSizeValue() {
		return sizeValue;
	}

	public void setSizeValue(String sizeValue) {
		this.sizeValue = sizeValue;
	}

	public String getEmailValue() {
		return emailValue;
	}

	public void setEmailValue(String emailValue) {
		this.emailValue = emailValue;
	}
}
