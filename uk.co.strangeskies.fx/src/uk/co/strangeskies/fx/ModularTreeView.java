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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javafx.scene.Node;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyEvent;
import javafx.util.Pair;
import uk.co.strangeskies.mathematics.graph.Graph;
import uk.co.strangeskies.mathematics.graph.GraphListeners;
import uk.co.strangeskies.mathematics.graph.processing.GraphProcessor;
import uk.co.strangeskies.reflection.token.TypedObject;

/**
 * An implementation of {@link TreeView} which allows for modular and extensible
 * specification of table structure.
 * 
 * @author Elias N Vasylenko
 */
public class ModularTreeView extends TreeView<TreeItemData<?>> {
	/*
	 * Graph of subtype relations so we can more easily find which contributions
	 * are the most specific.
	 */
	private final Graph<TreeContribution<?>, Object> contributions;
	private final Map<TreeContribution<?>, Integer> contributionRankings;
	private List<TreeContribution<?>> orderedContributions;

	private Comparator<Pair<TreeContribution<?>, Integer>> precedence;

	private TreeCellImpl selectedCell;

	/**
	 * Instantiate an empty tree view containing the
	 * {@link DefaultTreeCellContribution default cell contribution}, over a cell
	 * factory which instantiates an empty {@link TreeCellImpl}, and according to
	 * the {@link DefaultTreeContributionPrecedence default precedence}.
	 */
	public ModularTreeView() {
		FxmlLoadBuilder.build().object(this).load();
		setCellFactory(v -> new TreeCellImpl(this));

		contributionRankings = new HashMap<>();

		contributions = Graph
				.build()
				.vertices(Collections.<TreeContribution<?>> emptySet())
				.edgeFactory(Object::new)
				.direction(this::compareDescending)
				.addInternalListener(GraphListeners::vertexAdded, added -> {
					for (TreeContribution<?> existingVertex : added.graph().vertices())
						if (existingVertex != added.vertex())
							added.graph().edges().add(added.vertex(), existingVertex);

				})
				.create();

		setPrecedence(new DefaultTreeContributionPrecedence());

		addContribution(new DefaultTreeCellContribution());

		addEventFilter(KeyEvent.ANY, this::filterCellKeyEvent);

		setMinWidth(0);
		prefWidth(0);
	}

	protected synchronized void filterCellKeyEvent(KeyEvent event) {
		if (selectedCell != null) {
			Node target = selectedCell.getGraphic();

			if (target != null && event.getTarget() != target && !event.isConsumed()) {
				target.fireEvent(event.copyFor(event.getSource(), target));

				event.consume();
			}
		}
	}

	protected synchronized void fireCellKeyEvent(KeyEvent event) {
		if (selectedCell != null) {
			Node target = selectedCell.getGraphic();

			if (target != null) {

				if (event.getTarget() == target) {
					event.consume();

				} else if (!event.isConsumed()) {
					event = event.copyFor(event.getSource(), target);
					target.fireEvent(event);
				}
			}
		}
	}

	protected synchronized void setCellSelected(TreeCellImpl treeCellImpl, boolean selected) {
		if (selected) {
			selectedCell = treeCellImpl;
		} else if (selectedCell == treeCellImpl) {
			selectedCell = null;
		}
	}

	private int compareDescending(TreeContribution<?> first, TreeContribution<?> second) {
		return -getPrecedence().compare(rankedPair(first), rankedPair(second));
	}

	private Pair<TreeContribution<?>, Integer> rankedPair(TreeContribution<?> contribution) {
		return new Pair<>(contribution, contributionRankings.get(contribution));
	}

	/**
	 * @param precedence
	 *          the precedence by which to order tree contributions
	 */
	public void setPrecedence(Comparator<Pair<TreeContribution<?>, Integer>> precedence) {
		this.precedence = precedence;

		refreshContributions();
	}

	/**
	 * @return the precedence by which tree contributions are ordered
	 */
	public Comparator<Pair<TreeContribution<?>, Integer>> getPrecedence() {
		return precedence;
	}

	/**
	 * @param root
	 *          the root object supplemented with its exact generic type
	 */
	public void setRootData(TypedObject<?> root) {
		TreeItemImpl<?> rootItem = new TreeItemImpl<>(this, root);
		rootItem.setExpanded(true);
		setShowRoot(false);

		// add root
		setRoot(rootItem);

		refresh();
	}

	protected final TreeItemImpl<?> getRootImpl() {
		return (TreeItemImpl<?>) getRoot();
	}

	/**
	 * As per {@link #addContribution(TreeContribution, int)} with a ranking of 0.
	 * 
	 * @param contribution
	 *          the contribution to add to the view
	 * @return true if the contribution was successfully added, false otherwise
	 */
	public boolean addContribution(TreeContribution<?> contribution) {
		return addContribution(contribution, 0);
	}

	/**
	 * @param contribution
	 *          the contribution to add to the view
	 * @param ranking
	 *          the precedence ranking of the contribution
	 * @return true if the contribution was successfully added, false otherwise
	 */
	public boolean addContribution(TreeContribution<?> contribution, int ranking) {
		boolean added;

		if (contributionRankings.putIfAbsent(contribution, ranking) == null) {
			added = contributions.vertices().add(contribution);

			if (added) {
				refreshContributions();
			} else {
				contributionRankings.remove(contribution);
			}
		} else {
			added = false;
		}

		return added;
	}

	/**
	 * @param contribution
	 *          the contribution to remove from the view
	 * @return true if the contribution was successfully removed, false otherwise
	 */
	public boolean removeContribution(TreeContribution<?> contribution) {
		contributionRankings.remove(contribution);
		boolean removed = contributions.vertices().remove(contribution);

		if (removed) {
			refreshContributions();
		}

		return removed;
	}

	private void refreshContributions() {
		orderedContributions = GraphProcessor.over(contributions).processEager();
	}

	/**
	 * @return all contributions added to the view in order of from most to least
	 *         preferred
	 */
	public Stream<TreeContribution<?>> getContributions() {
		return orderedContributions.stream();
	}

	@Override
	public void refresh() {
		getRootImpl().getData().refresh(true);
	}
}
