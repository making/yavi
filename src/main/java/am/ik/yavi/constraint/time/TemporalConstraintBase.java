package am.ik.yavi.constraint.time;

import am.ik.yavi.constraint.base.ConstraintBase;
import am.ik.yavi.core.Constraint;

import java.time.temporal.Temporal;

/**
 * This class represents the base class for all the Temporal objects that may need to be
 * validated.
 *
 * @author Diego Krupitza
 */
public abstract class TemporalConstraintBase<T, V extends Temporal, C extends Constraint<T, V, C>>
		extends ConstraintBase<T, V, C> {
}
