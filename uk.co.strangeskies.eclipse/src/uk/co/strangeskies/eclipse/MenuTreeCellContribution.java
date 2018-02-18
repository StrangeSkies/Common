/*
 * Copyright (C) 2018 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.eclipse.
 *
 * uk.co.strangeskies.eclipse is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.eclipse is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.eclipse;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.services.EMenuService;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import uk.co.strangeskies.fx.TreeCellContribution;
import uk.co.strangeskies.fx.TreeItemData;

/**
 * A tree cell contribution intended to be supplied via
 * {@link EclipseTreeContribution} so as to be injected according to an eclipse
 * context.
 * <p>
 * This contribution registers an E4 popup menu to the cell, which can be
 * activated via right click or the context menu key.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of data of applicable nodes
 */
public abstract class MenuTreeCellContribution<T> implements TreeCellContribution<T>, EclipseTreeContribution<T> {
	@Inject
	EMenuService menuService;

	private final String menuId;
	private ContextMenu menu;

	/**
	 * @param menuId
	 *          the ID of the popup menu in the E4 model
	 */
	public MenuTreeCellContribution(String menuId) {
		this.menuId = menuId;
	}

	@SuppressWarnings("javadoc")
	@PostConstruct
	public void configureMenu() {
		Control menuControl = new Control() {};

		menuService.registerContextMenu(menuControl, menuId);

		menu = menuControl.getContextMenu();
		menu.addEventHandler(KeyEvent.ANY, Event::consume);
	}

	@Override
	public <U extends T> Node configureCell(TreeItemData<U> data, Node content) {
		content.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
			menu.show(content, event.getScreenX(), event.getScreenY());
			event.consume();
		});
		content.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
			menu.hide();
		});

		return content;
	}
}
