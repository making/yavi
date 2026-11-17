package am.ik.yavi.runtime.guest;

/**
 * Execution budget for example programs. The runtime rewrites the compiled example
 * classes so that every method entry and every jump calls {@link #tick()}; a runaway loop
 * therefore ends with a {@link BudgetExceededError} instead of hanging the page.
 * <p>
 * The class is compiled into the image and also written into the guest class path that
 * Espresso loads, so instrumented example code can link against it.
 */
public final class Budget {

	/** Steps a single run may consume. Real examples use a few thousand. */
	public static final long DEFAULT_STEPS = 2_000_000L;

	private static long remaining = DEFAULT_STEPS;

	private static long limit = DEFAULT_STEPS;

	private Budget() {
	}

	/**
	 * Starts a new budget. A fresh Espresso context loads its own copy of this class, so
	 * this is mostly for clarity when a context is reused.
	 * @param steps the number of steps the program may take
	 */
	public static void reset(long steps) {
		limit = steps;
		remaining = steps;
	}

	/** Charges one step to the current program. */
	public static void tick() {
		if (remaining <= 0) {
			// Do not throw again while unwinding.
			remaining = Long.MAX_VALUE;
			throw new BudgetExceededError(
					"Execution stopped after " + limit + " steps; the program does not seem to finish");
		}
		remaining--;
	}

	/** Signals that the program exhausted its step budget. */
	public static final class BudgetExceededError extends Error {

		BudgetExceededError(String message) {
			super(message);
		}

	}

}
