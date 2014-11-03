package uk.co.strangeskies.mathematics.logic;

import uk.co.strangeskies.utilities.Self;

public interface BooleanCombinationBehaviour<S extends BooleanCombinationBehaviour<S, T> & Self<? extends S>, T>
		extends ANDable<S, T>, NANDable<S, T>, NORable<S, T>, ORable<S, T>,
		XNORable<S, T>, XORable<S, T> {
}
