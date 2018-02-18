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

import static java.util.Collections.emptyMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import uk.co.strangeskies.fx.TreeCellContribution;
import uk.co.strangeskies.fx.TreeItemData;

/**
 * A tree cell contribution intended to be supplied via
 * {@link EclipseTreeContribution} so as to be injected according to an eclipse
 * context.
 * <p>
 * This contribution registers an E4 command to the cell, which can be activated
 * via double click or the enter key.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of data of applicable nodes
 */
public abstract class CommandTreeCellContribution<T> implements TreeCellContribution<T>, EclipseTreeContribution<T> {
	@Inject
	EHandlerService handlerService;

	private final String commandId;
	private ParameterizedCommand command;

	/**
	 * @param commandId
	 *          the ID of the command in the E4 model
	 */
	public CommandTreeCellContribution(String commandId) {
		this.commandId = commandId;
	}

	@SuppressWarnings("javadoc")
	@PostConstruct
	public void configureCommand(ECommandService commandService) {
		command = commandService.createCommand(commandId, emptyMap());
	}

	@Override
	public <U extends T> Node configureCell(TreeItemData<U> data, Node content) {
		content.addEventHandler(MouseEvent.ANY, event -> {
			if (event.getClickCount() == 2 && event.getButton().equals(MouseButton.PRIMARY)) {
				event.consume();

				if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
					executeCommand(data, content);
				}
			}
		});

		content.addEventHandler(KeyEvent.ANY, event -> {
			if (event.getCode() == KeyCode.ENTER) {
				event.consume();

				if (event.getEventType().equals(KeyEvent.KEY_PRESSED)) {
					executeCommand(data, content);
				}
			}
		});

		return content;
	}

	private void executeCommand(TreeItemData<?> data, Node node) {
		handlerService.executeHandler(command);
	}
}
