/*
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.skript.lang;

import java.util.List;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.parser.ParserInstance;

/**
 * Represents a section of a trigger, e.g. a conditional or a loop
 * 
 * @author Peter Güttinger
 * @see Conditional
 * @see Loop
 */
public abstract class TriggerSection extends TriggerItem {
	
	@Nullable
	private TriggerItem first = null;
	@Nullable
	protected TriggerItem last = null;
	
	/**
	 * Reserved for new Trigger(...)
	 */
	protected TriggerSection(final List<TriggerItem> items) {
		setTriggerItems(items);
	}
	
	protected TriggerSection(final SectionNode node, final ParserInstance pi) {
		pi.currentSections.add(this);
		try {
			setTriggerItems(pi.loadItems(node));
		} finally {
			pi.currentSections.remove(pi.currentSections.size() - 1);
		}
	}
	
	/**
	 * Important when using this constructor: set the items with {@link #setTriggerItems(List)}!
	 */
	protected TriggerSection() {}
	
	/**
	 * Remember to add this section to {@link LoaderInstance#currentSections} before parsing child elements!
	 * 
	 * <pre>
	 * LoaderInstance.currentSections.add(this);
	 * setTriggerItems(LoaderInstance.loadItems(node));
	 * LoaderInstance.currentSections.remove(LoaderInstance.currentSections.size() - 1);
	 * </pre>
	 * 
	 * @param items
	 */
	protected void setTriggerItems(final List<TriggerItem> items) {
		if (!items.isEmpty()) {
			first = items.get(0);
			(last = items.get(items.size() - 1))
					.setNext(getNext());
		}
		for (final TriggerItem item : items) {
			item.setParent(this);
		}
	}
	
	@Override
	public TriggerSection setNext(final @Nullable TriggerItem next) {
		super.setNext(next);
		if (last != null)
			last.setNext(next);
		return this;
	}
	
	@Override
	public TriggerSection setParent(@Nullable final TriggerSection parent) {
		super.setParent(parent);
		return this;
	}
	
	@Override
	protected final boolean run(final Event e) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	@Nullable
	protected abstract TriggerItem walk(Event e);
	
	@Nullable
	protected final TriggerItem walk(final Event e, final boolean run) {
		debug(e, run);
		if (run && first != null) {
			return first;
		} else {
			return getNext();
		}
	}
	
}