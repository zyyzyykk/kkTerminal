/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
// This is a facade for the observable implementation. Only import from here!
export { observableValue, disposableObservableValue, transaction, subtransaction } from './observableInternal/base.js';
export { derived, derivedOpts, derivedHandleChanges, derivedWithStore } from './observableInternal/derived.js';
export { autorun, autorunHandleChanges, autorunWithStore, autorunOpts } from './observableInternal/autorun.js';
export { constObservable, derivedObservableWithCache, recomputeInitiallyAndOnChange, observableFromEvent, observableSignal, observableSignalFromEvent, waitForState } from './observableInternal/utils.js';
import { ConsoleObservableLogger, setLogger } from './observableInternal/logging.js';
const enableLogging = false;
if (enableLogging) {
    setLogger(new ConsoleObservableLogger());
}
