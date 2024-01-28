import { createDecorator } from '../../instantiation/common/instantiation.js';
import { localize } from '../../../nls.js';
export const IAudioCueService = createDecorator('audioCue');
/**
 * Corresponds to the audio files in ./media.
*/
export class Sound {
    static register(options) {
        const sound = new Sound(options.fileName);
        return sound;
    }
    constructor(fileName) {
        this.fileName = fileName;
    }
}
Sound.error = Sound.register({ fileName: 'error.mp3' });
Sound.warning = Sound.register({ fileName: 'warning.mp3' });
Sound.foldedArea = Sound.register({ fileName: 'foldedAreas.mp3' });
Sound.break = Sound.register({ fileName: 'break.mp3' });
Sound.quickFixes = Sound.register({ fileName: 'quickFixes.mp3' });
Sound.taskCompleted = Sound.register({ fileName: 'taskCompleted.mp3' });
Sound.taskFailed = Sound.register({ fileName: 'taskFailed.mp3' });
Sound.terminalBell = Sound.register({ fileName: 'terminalBell.mp3' });
Sound.diffLineInserted = Sound.register({ fileName: 'diffLineInserted.mp3' });
Sound.diffLineDeleted = Sound.register({ fileName: 'diffLineDeleted.mp3' });
Sound.diffLineModified = Sound.register({ fileName: 'diffLineModified.mp3' });
Sound.chatRequestSent = Sound.register({ fileName: 'chatRequestSent.mp3' });
Sound.chatResponsePending = Sound.register({ fileName: 'chatResponsePending.mp3' });
Sound.chatResponseReceived1 = Sound.register({ fileName: 'chatResponseReceived1.mp3' });
Sound.chatResponseReceived2 = Sound.register({ fileName: 'chatResponseReceived2.mp3' });
Sound.chatResponseReceived3 = Sound.register({ fileName: 'chatResponseReceived3.mp3' });
Sound.chatResponseReceived4 = Sound.register({ fileName: 'chatResponseReceived4.mp3' });
Sound.clear = Sound.register({ fileName: 'clear.mp3' });
Sound.save = Sound.register({ fileName: 'save.mp3' });
Sound.format = Sound.register({ fileName: 'format.mp3' });
export class SoundSource {
    constructor(randomOneOf) {
        this.randomOneOf = randomOneOf;
    }
}
export class AudioCue {
    static register(options) {
        const soundSource = new SoundSource('randomOneOf' in options.sound ? options.sound.randomOneOf : [options.sound]);
        const audioCue = new AudioCue(soundSource, options.name, options.settingsKey);
        AudioCue._audioCues.add(audioCue);
        return audioCue;
    }
    constructor(sound, name, settingsKey) {
        this.sound = sound;
        this.name = name;
        this.settingsKey = settingsKey;
    }
}
AudioCue._audioCues = new Set();
AudioCue.error = AudioCue.register({
    name: localize('audioCues.lineHasError.name', 'Error on Line'),
    sound: Sound.error,
    settingsKey: 'audioCues.lineHasError',
});
AudioCue.warning = AudioCue.register({
    name: localize('audioCues.lineHasWarning.name', 'Warning on Line'),
    sound: Sound.warning,
    settingsKey: 'audioCues.lineHasWarning',
});
AudioCue.foldedArea = AudioCue.register({
    name: localize('audioCues.lineHasFoldedArea.name', 'Folded Area on Line'),
    sound: Sound.foldedArea,
    settingsKey: 'audioCues.lineHasFoldedArea',
});
AudioCue.break = AudioCue.register({
    name: localize('audioCues.lineHasBreakpoint.name', 'Breakpoint on Line'),
    sound: Sound.break,
    settingsKey: 'audioCues.lineHasBreakpoint',
});
AudioCue.inlineSuggestion = AudioCue.register({
    name: localize('audioCues.lineHasInlineSuggestion.name', 'Inline Suggestion on Line'),
    sound: Sound.quickFixes,
    settingsKey: 'audioCues.lineHasInlineSuggestion',
});
AudioCue.terminalQuickFix = AudioCue.register({
    name: localize('audioCues.terminalQuickFix.name', 'Terminal Quick Fix'),
    sound: Sound.quickFixes,
    settingsKey: 'audioCues.terminalQuickFix',
});
AudioCue.onDebugBreak = AudioCue.register({
    name: localize('audioCues.onDebugBreak.name', 'Debugger Stopped on Breakpoint'),
    sound: Sound.break,
    settingsKey: 'audioCues.onDebugBreak',
});
AudioCue.noInlayHints = AudioCue.register({
    name: localize('audioCues.noInlayHints', 'No Inlay Hints on Line'),
    sound: Sound.error,
    settingsKey: 'audioCues.noInlayHints'
});
AudioCue.taskCompleted = AudioCue.register({
    name: localize('audioCues.taskCompleted', 'Task Completed'),
    sound: Sound.taskCompleted,
    settingsKey: 'audioCues.taskCompleted'
});
AudioCue.taskFailed = AudioCue.register({
    name: localize('audioCues.taskFailed', 'Task Failed'),
    sound: Sound.taskFailed,
    settingsKey: 'audioCues.taskFailed'
});
AudioCue.terminalCommandFailed = AudioCue.register({
    name: localize('audioCues.terminalCommandFailed', 'Terminal Command Failed'),
    sound: Sound.error,
    settingsKey: 'audioCues.terminalCommandFailed'
});
AudioCue.terminalBell = AudioCue.register({
    name: localize('audioCues.terminalBell', 'Terminal Bell'),
    sound: Sound.terminalBell,
    settingsKey: 'audioCues.terminalBell'
});
AudioCue.notebookCellCompleted = AudioCue.register({
    name: localize('audioCues.notebookCellCompleted', 'Notebook Cell Completed'),
    sound: Sound.taskCompleted,
    settingsKey: 'audioCues.notebookCellCompleted'
});
AudioCue.notebookCellFailed = AudioCue.register({
    name: localize('audioCues.notebookCellFailed', 'Notebook Cell Failed'),
    sound: Sound.taskFailed,
    settingsKey: 'audioCues.notebookCellFailed'
});
AudioCue.diffLineInserted = AudioCue.register({
    name: localize('audioCues.diffLineInserted', 'Diff Line Inserted'),
    sound: Sound.diffLineInserted,
    settingsKey: 'audioCues.diffLineInserted'
});
AudioCue.diffLineDeleted = AudioCue.register({
    name: localize('audioCues.diffLineDeleted', 'Diff Line Deleted'),
    sound: Sound.diffLineDeleted,
    settingsKey: 'audioCues.diffLineDeleted'
});
AudioCue.diffLineModified = AudioCue.register({
    name: localize('audioCues.diffLineModified', 'Diff Line Modified'),
    sound: Sound.diffLineModified,
    settingsKey: 'audioCues.diffLineModified'
});
AudioCue.chatRequestSent = AudioCue.register({
    name: localize('audioCues.chatRequestSent', 'Chat Request Sent'),
    sound: Sound.chatRequestSent,
    settingsKey: 'audioCues.chatRequestSent'
});
AudioCue.chatResponseReceived = AudioCue.register({
    name: localize('audioCues.chatResponseReceived', 'Chat Response Received'),
    settingsKey: 'audioCues.chatResponseReceived',
    sound: {
        randomOneOf: [
            Sound.chatResponseReceived1,
            Sound.chatResponseReceived2,
            Sound.chatResponseReceived3,
            Sound.chatResponseReceived4
        ]
    }
});
AudioCue.chatResponsePending = AudioCue.register({
    name: localize('audioCues.chatResponsePending', 'Chat Response Pending'),
    sound: Sound.chatResponsePending,
    settingsKey: 'audioCues.chatResponsePending'
});
AudioCue.clear = AudioCue.register({
    name: localize('audioCues.clear', 'Clear'),
    sound: Sound.clear,
    settingsKey: 'audioCues.clear'
});
AudioCue.save = AudioCue.register({
    name: localize('audioCues.save', 'Save'),
    sound: Sound.save,
    settingsKey: 'audioCues.save'
});
AudioCue.format = AudioCue.register({
    name: localize('audioCues.format', 'Format'),
    sound: Sound.format,
    settingsKey: 'audioCues.format'
});
