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

import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.LOOSE_COMPATIBILILTY;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypedObject;

/**
 * Users should not need to extend this class. Item specific behavior should be
 * handled by extending {@link TreeChildContribution} for each type of node
 * which can appear in a tree.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of data for this tree item
 */
public class TreeItemImpl<T> extends TreeItem<TreeItemData<?>> {
	private final ModularTreeView treeView;
	private final TreeItemImpl<?> parent;

	private final Map<TypedObject<?>, TreeItemImpl<?>> childTreeItems = new HashMap<>();
	private boolean childrenCalculated;

	TreeItemImpl(ModularTreeView treeView, TypedObject<T> data) {
		this(treeView, data, null);
	}

	TreeItemImpl(ModularTreeView treeView, TypedObject<T> data, TreeItemImpl<?> parent) {
		this.treeView = treeView;
		this.parent = parent;

		setValue(new TreeItemDataImpl<>(data));

		rebuildChildren();
	}

	protected boolean hasChildrenContributions() {
		return getData()
				.contributions(new TypeToken<TreeChildContribution<? super T>>() {})
				.anyMatch(c -> c.hasChildren(getData()));
	}

	protected List<TypedObject<?>> getChildrenContributions() {
		return getData()
				.contributions(new TypeToken<TreeChildContribution<? super T>>() {})
				.flatMap(c -> c.getChildren(getData()))
				.distinct()
				.collect(Collectors.toList());
	}

	/**
	 * @return the {@link TreeItemDataImpl tree item data} for this tree node
	 */
	public TreeItemData<T> getData() {
		return getDataImpl();
	}

	@SuppressWarnings("unchecked")
	protected TreeItemDataImpl<T> getDataImpl() {
		return (TreeItemDataImpl<T>) getValue();
	}

	@Override
	public ObservableList<TreeItem<TreeItemData<?>>> getChildren() {
		if (!childrenCalculated) {
			rebuildChildren();
		}

		return super.getChildren();
	}

	private void rebuild(boolean recursive) {
		TreeItemData<?> data = getValue();
		setValue(null);
		setValue(data);

		if (recursive) {
			rebuildChildren();
		}
	}

	protected void rebuildChildren() {
		getDataImpl().refreshContributions();

		boolean hasChildren = hasChildrenContributions();

		List<TreeItem<TreeItemData<?>>> childrenItems;

		if (hasChildren) {
			if (isExpanded()) {
				List<TypedObject<?>> childrenData = getChildrenContributions();

				// remove outdated TreeItemImpl children
				childTreeItems.keySet().retainAll(childrenData);

				childrenItems = childrenData.stream().map(i -> {
					TreeItemImpl<?> treeItem = childTreeItems.get(i);

					if (treeItem == null) {
						treeItem = new TreeItemImpl<>(treeView, i, this);
						childTreeItems.put(i, treeItem);
					} else {
						treeItem.rebuild(true);
					}

					return treeItem;
				}).collect(toList());

				childrenCalculated = true;
			} else {
				childrenItems = Arrays.asList(new TreeItem<>());
				childrenCalculated = false;
			}
		} else {
		  setExpanded(false);
			childTreeItems.clear();

			childrenItems = Collections.emptyList();
			childrenCalculated = true;
		}

		super.getChildren().setAll(childrenItems);
	}

	/**
	 * An implementation of {@link TreeItemData} for {@link TreeItemImpl}.
	 * 
	 * @author Elias N Vasylenko
	 *
	 * @param <U>
	 *          the type of the tree item data
	 */
	public class TreeItemDataImpl<U> implements TreeItemData<U> {
		private final TypedObject<U> data;

		private List<TreeContribution<? super U>> itemContributions;

		/**
		 * @param data
		 *          the typed data for this tree item data object
		 */
		public TreeItemDataImpl(TypedObject<U> data) {
			this.data = data;
			refreshContributions();
		}

		@Override
		public ModularTreeView treeView() {
			return treeView;
		}

		@Override
		public TypedObject<U> typedData() {
			return data;
		}

		@Override
		public Optional<TreeItemData<?>> parent() {
			return Optional.ofNullable(parent).map(TreeItemImpl::getData);
		}

		@SuppressWarnings("unchecked")
		protected void refreshContributions() {
			itemContributions = treeView
					.getContributions()
					.filter(c -> c.getDataType().satisfiesConstraintFrom(LOOSE_COMPATIBILILTY, type()))
					.map(c -> (TreeContribution<? super U>) c)
					.filter(c -> c.appliesTo(this))
					.collect(Collectors.toList());
		}

		@Override
		public Stream<TreeContribution<? super U>> contributions() {
			return itemContributions.stream();
		}

		@Override
		public <V extends TreeContribution<? super U>> Stream<V> contributions(TypeToken<V> type) {
			return itemContributions
					.stream()
					.filter(c -> forClass(type.getErasedType()).satisfiesConstraintFrom(LOOSE_COMPATIBILILTY, c.getClass()))
					.map(type::cast);
		}

		@Override
		public void refresh(boolean recursive) {
			TreeItem<TreeItemData<?>> selected = treeView.getSelectionModel().getSelectedItem();
			treeView.getSelectionModel().clearSelection();

			TreeItem<TreeItemData<?>> focused = treeView.getFocusModel().getFocusedItem();
			treeView.getFocusModel().focus(-1);

			FxUtilities.runNow(() -> {
				rebuild(recursive);

				for (int i = 0; i < treeView.getExpandedItemCount(); i++) {
					TreeItem<?> treeItem = treeView.getTreeItem(i);

					if (selected == treeItem) {
						treeView.getSelectionModel().clearAndSelect(i);
					}

					if (focused == treeItem) {
						treeView.getFocusModel().focus(i);
					}
				}
			});
		}
	}
}
