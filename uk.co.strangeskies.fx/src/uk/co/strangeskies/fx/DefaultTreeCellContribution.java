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
 * This file is part of uk.co.strangeskies.fx.
 *
 * uk.co.strangeskies.fx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.fx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.fx;

import static java.util.Optional.ofNullable;

import java.util.Objects;
import java.util.function.Supplier;

import javafx.scene.Node;
import uk.co.strangeskies.reflection.token.TypeToken;

/**
 * The default tree cell contribution. This configures a cell with basic text
 * content derived from any applicable {@link TreeTextContribution text
 * contributions}.
 * 
 * @author Elias N Vasylenko
 */
public class DefaultTreeCellContribution implements TreeCellContribution<Object> {
	class CellTexts<U> {
		private final Supplier<String> text;
		private final Supplier<String> supplementalText;

		public CellTexts(TreeTextContribution<? super U> contribution, TreeItemData<U> data) {
			this.text = () -> contribution.getText(data);
			this.supplementalText = () -> contribution.getSupplementalText(data);
		}

		public CellTexts(Supplier<String> text, Supplier<String> supplementalText) {
			this.text = text;
			this.supplementalText = supplementalText;
		}

		public CellTexts<U> override(CellTexts<U> texts) {
			return new CellTexts<>(
					() -> ofNullable(text.get()).orElse(texts.getText()),
					() -> ofNullable(supplementalText.get()).orElse(texts.getSupplementalText()));
		}

		public String getText() {
			return text.get();
		}

		public String getSupplementalText() {
			return supplementalText.get();
		}
	}

	@Override
	public <U> Node configureCell(TreeItemData<U> data, Node ignore) {
		CellTexts<U> texts = data
				.contributions(new TypeToken<TreeTextContribution<? super U>>() {})
				.map(c -> new CellTexts<>(c, data))
				.reduce(CellTexts::override)
				.map(t -> t.override(getDefaultTexts(data)))
				.orElseGet(() -> getDefaultTexts(data));

		return new DefaultTreeCellContent(texts.getText(), texts.getSupplementalText());
	}

	private <U> CellTexts<U> getDefaultTexts(TreeItemData<U> data) {
		return new CellTexts<>(() -> null, () -> Objects.toString(data.data()));
	}
}
