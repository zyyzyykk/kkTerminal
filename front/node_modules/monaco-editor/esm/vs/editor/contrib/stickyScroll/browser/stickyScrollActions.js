/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import { EditorAction2 } from '../../../browser/editorExtensions.js';
import { localize } from '../../../../nls.js';
import { Categories } from '../../../../platform/action/common/actionCommonCategories.js';
import { Action2, MenuId } from '../../../../platform/actions/common/actions.js';
import { IConfigurationService } from '../../../../platform/configuration/common/configuration.js';
import { ContextKeyExpr } from '../../../../platform/contextkey/common/contextkey.js';
import { EditorContextKeys } from '../../../common/editorContextKeys.js';
import { StickyScrollController } from './stickyScrollController.js';
export class ToggleStickyScroll extends Action2 {
    constructor() {
        super({
            id: 'editor.action.toggleStickyScroll',
            title: {
                value: localize('toggleStickyScroll', "Toggle Sticky Scroll"),
                mnemonicTitle: localize({ key: 'mitoggleStickyScroll', comment: ['&& denotes a mnemonic'] }, "&&Toggle Sticky Scroll"),
                original: 'Toggle Sticky Scroll',
            },
            category: Categories.View,
            toggled: {
                condition: ContextKeyExpr.equals('config.editor.stickyScroll.enabled', true),
                title: localize('stickyScroll', "Sticky Scroll"),
                mnemonicTitle: localize({ key: 'miStickyScroll', comment: ['&& denotes a mnemonic'] }, "&&Sticky Scroll"),
            },
            menu: [
                { id: MenuId.CommandPalette },
                { id: MenuId.MenubarAppearanceMenu, group: '4_editor', order: 3 },
                { id: MenuId.StickyScrollContext }
            ]
        });
    }
    async run(accessor) {
        const configurationService = accessor.get(IConfigurationService);
        const newValue = !configurationService.getValue('editor.stickyScroll.enabled');
        return configurationService.updateValue('editor.stickyScroll.enabled', newValue);
    }
}
const weight = 100 /* KeybindingWeight.EditorContrib */;
export class FocusStickyScroll extends EditorAction2 {
    constructor() {
        super({
            id: 'editor.action.focusStickyScroll',
            title: {
                value: localize('focusStickyScroll', "Focus Sticky Scroll"),
                mnemonicTitle: localize({ key: 'mifocusStickyScroll', comment: ['&& denotes a mnemonic'] }, "&&Focus Sticky Scroll"),
                original: 'Focus Sticky Scroll',
            },
            precondition: ContextKeyExpr.and(ContextKeyExpr.has('config.editor.stickyScroll.enabled'), EditorContextKeys.stickyScrollVisible),
            menu: [
                { id: MenuId.CommandPalette },
            ]
        });
    }
    runEditorCommand(_accessor, editor) {
        var _a;
        (_a = StickyScrollController.get(editor)) === null || _a === void 0 ? void 0 : _a.focus();
    }
}
export class SelectNextStickyScrollLine extends EditorAction2 {
    constructor() {
        super({
            id: 'editor.action.selectNextStickyScrollLine',
            title: {
                value: localize('selectNextStickyScrollLine.title', "Select next sticky scroll line"),
                original: 'Select next sticky scroll line'
            },
            precondition: EditorContextKeys.stickyScrollFocused.isEqualTo(true),
            keybinding: {
                weight,
                primary: 18 /* KeyCode.DownArrow */
            }
        });
    }
    runEditorCommand(_accessor, editor) {
        var _a;
        (_a = StickyScrollController.get(editor)) === null || _a === void 0 ? void 0 : _a.focusNext();
    }
}
export class SelectPreviousStickyScrollLine extends EditorAction2 {
    constructor() {
        super({
            id: 'editor.action.selectPreviousStickyScrollLine',
            title: {
                value: localize('selectPreviousStickyScrollLine.title', "Select previous sticky scroll line"),
                original: 'Select previous sticky scroll line'
            },
            precondition: EditorContextKeys.stickyScrollFocused.isEqualTo(true),
            keybinding: {
                weight,
                primary: 16 /* KeyCode.UpArrow */
            }
        });
    }
    runEditorCommand(_accessor, editor) {
        var _a;
        (_a = StickyScrollController.get(editor)) === null || _a === void 0 ? void 0 : _a.focusPrevious();
    }
}
export class GoToStickyScrollLine extends EditorAction2 {
    constructor() {
        super({
            id: 'editor.action.goToFocusedStickyScrollLine',
            title: {
                value: localize('goToFocusedStickyScrollLine.title', "Go to focused sticky scroll line"),
                original: 'Go to focused sticky scroll line'
            },
            precondition: EditorContextKeys.stickyScrollFocused.isEqualTo(true),
            keybinding: {
                weight,
                primary: 3 /* KeyCode.Enter */
            }
        });
    }
    runEditorCommand(_accessor, editor) {
        var _a;
        (_a = StickyScrollController.get(editor)) === null || _a === void 0 ? void 0 : _a.goToFocused();
    }
}
export class SelectEditor extends EditorAction2 {
    constructor() {
        super({
            id: 'editor.action.selectEditor',
            title: {
                value: localize('selectEditor.title', "Select Editor"),
                original: 'Select Editor'
            },
            precondition: EditorContextKeys.stickyScrollFocused.isEqualTo(true),
            keybinding: {
                weight,
                primary: 9 /* KeyCode.Escape */
            }
        });
    }
    runEditorCommand(_accessor, editor) {
        var _a;
        (_a = StickyScrollController.get(editor)) === null || _a === void 0 ? void 0 : _a.selectEditor();
    }
}
