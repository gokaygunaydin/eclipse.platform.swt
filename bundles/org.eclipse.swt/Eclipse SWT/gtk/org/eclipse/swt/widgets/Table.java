/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.widgets;


import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.cairo.*;
import org.eclipse.swt.internal.gtk.*;

/**
 * Instances of this class implement a selectable user interface
 * object that displays a list of images and strings and issues
 * notification when selected.
 * <p>
 * The item children that may be added to instances of this class
 * must be of type <code>TableItem</code>.
 * </p><p>
 * Style <code>VIRTUAL</code> is used to create a <code>Table</code> whose
 * <code>TableItem</code>s are to be populated by the client on an on-demand basis
 * instead of up-front.  This can provide significant performance improvements for
 * tables that are very large or for which <code>TableItem</code> population is
 * expensive (for example, retrieving values from an external source).
 * </p><p>
 * Here is an example of using a <code>Table</code> with style <code>VIRTUAL</code>:
 * <code><pre>
 *  final Table table = new Table (parent, SWT.VIRTUAL | SWT.BORDER);
 *  table.setItemCount (1000000);
 *  table.addListener (SWT.SetData, new Listener () {
 *      public void handleEvent (Event event) {
 *          TableItem item = (TableItem) event.item;
 *          int index = table.indexOf (item);
 *          item.setText ("Item " + index);
 *          System.out.println (item.getText ());
 *      }
 *  });
 * </pre></code>
 * </p><p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not normally make sense to add <code>Control</code> children to
 * it, or set a layout on it, unless implementing something like a cell
 * editor.
 * </p><p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SINGLE, MULTI, CHECK, FULL_SELECTION, HIDE_SELECTION, VIRTUAL, NO_SCROLL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection, DefaultSelection, SetData, MeasureItem, EraseItem, PaintItem</dd>
 * </dl>
 * </p><p>
 * Note: Only one of the styles SINGLE, and MULTI may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#table">Table, TableItem, TableColumn snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class Table extends Composite {
	long /*int*/ modelHandle, checkRenderer;
	int itemCount, columnCount, lastIndexOf, sortDirection;
	long /*int*/ ignoreCell;
	TableItem [] items;
	TableColumn [] columns;
	TableItem currentItem;
	TableColumn sortColumn;
	ImageList imageList, headerImageList;
	boolean firstCustomDraw;
	int drawState, drawFlags;
	GdkColor drawForeground;
	GdkRGBA background;
	boolean ownerDraw, ignoreSize, ignoreAccessibility, pixbufSizeSet;

	static final int CHECKED_COLUMN = 0;
	static final int GRAYED_COLUMN = 1;
	static final int FOREGROUND_COLUMN = 2;
	static final int BACKGROUND_COLUMN = 3;
	static final int FONT_COLUMN = 4;
	static final int FIRST_COLUMN = FONT_COLUMN + 1;
	static final int CELL_PIXBUF = 0;
	static final int CELL_TEXT = 1;
	static final int CELL_FOREGROUND = 2;
	static final int CELL_BACKGROUND = 3;
	static final int CELL_FONT = 4;
	static final int CELL_TYPES = CELL_FONT + 1;

/**
 * Constructs a new instance of this class given its parent
 * and a style value describing its behavior and appearance.
 * <p>
 * The style value is either one of the style constants defined in
 * class <code>SWT</code> which is applicable to instances of this
 * class, or must be built by <em>bitwise OR</em>'ing together
 * (that is, using the <code>int</code> "|" operator) two or more
 * of those <code>SWT</code> style constants. The class description
 * lists the style constants that are applicable to the class.
 * Style bits are also inherited from superclasses.
 * </p>
 *
 * @param parent a composite control which will be the parent of the new instance (cannot be null)
 * @param style the style of control to construct
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 *
 * @see SWT#SINGLE
 * @see SWT#MULTI
 * @see SWT#CHECK
 * @see SWT#FULL_SELECTION
 * @see SWT#HIDE_SELECTION
 * @see SWT#VIRTUAL
 * @see SWT#NO_SCROLL
 * @see Widget#checkSubclass
 * @see Widget#getStyle
 */
public Table (Composite parent, int style) {
	super (parent, checkStyle (style));
}

@Override
void _addListener (int eventType, Listener listener) {
	super._addListener (eventType, listener);
	if (!ownerDraw) {
		switch (eventType) {
			case SWT.MeasureItem:
			case SWT.EraseItem:
			case SWT.PaintItem:
				ownerDraw = true;
				recreateRenderers ();
				break;
		}
	}
}

TableItem _getItem (int index) {
	if ((style & SWT.VIRTUAL) == 0) return items [index];
	if (items [index] != null) return items [index];
	return items [index] = new TableItem (this, SWT.NONE, index, false);
}

static int checkStyle (int style) {
	/*
	* Feature in Windows.  Even when WS_HSCROLL or
	* WS_VSCROLL is not specified, Windows creates
	* trees and tables with scroll bars.  The fix
	* is to set H_SCROLL and V_SCROLL.
	*
	* NOTE: This code appears on all platforms so that
	* applications have consistent scroll bar behavior.
	*/
	if ((style & SWT.NO_SCROLL) == 0) {
		style |= SWT.H_SCROLL | SWT.V_SCROLL;
	}
	/* GTK is always FULL_SELECTION */
	style |= SWT.FULL_SELECTION;
	return checkBits (style, SWT.SINGLE, SWT.MULTI, 0, 0, 0, 0);
}

@Override
long /*int*/ cellDataProc (long /*int*/ tree_column, long /*int*/ cell, long /*int*/ tree_model, long /*int*/ iter, long /*int*/ data) {
	if (cell == ignoreCell) return 0;
	long /*int*/ path = OS.gtk_tree_model_get_path (tree_model, iter);
	int [] index = new int [1];
	OS.memmove (index, OS.gtk_tree_path_get_indices (path), 4);
	TableItem item = _getItem (index[0]);
	OS.gtk_tree_path_free (path);
	if (item != null) OS.g_object_set_qdata (cell, Display.SWT_OBJECT_INDEX2, item.handle);
	boolean isPixbuf = OS.GTK_IS_CELL_RENDERER_PIXBUF (cell);
	boolean isText = OS.GTK_IS_CELL_RENDERER_TEXT (cell);
	if (isText && OS.GTK3) {
		OS.gtk_cell_renderer_set_fixed_size (cell, -1, -1);
	}
	if (!(isPixbuf || isText)) return 0;
	int modelIndex = -1;
	boolean customDraw = false;
	if (columnCount == 0) {
		modelIndex = Table.FIRST_COLUMN;
		customDraw = firstCustomDraw;
	} else {
		TableColumn column = (TableColumn) display.getWidget (tree_column);
		if (column != null) {
			modelIndex = column.modelIndex;
			customDraw = column.customDraw;
		}
	}
	if (modelIndex == -1) return 0;
	boolean setData = false;
	if ((style & SWT.VIRTUAL) != 0) {
		if (!item.cached) {
			lastIndexOf = index[0];
			setData = checkData (item);
		}
	}
	long /*int*/ [] ptr = new long /*int*/ [1];
	if (setData) {
		ptr [0] = 0;
		if (isPixbuf) {
			OS.gtk_tree_model_get (tree_model, iter, modelIndex + CELL_PIXBUF, ptr, -1);
			OS.g_object_set (cell, OS.GTK3 ? OS.gicon : OS.pixbuf, ptr [0], 0);
			if (ptr [0] != 0) OS.g_object_unref (ptr [0]);
		} else {
			OS.gtk_tree_model_get (tree_model, iter, modelIndex + CELL_TEXT, ptr, -1);
			if (ptr [0] != 0) {
				OS.g_object_set (cell, OS.text, ptr [0], 0);
				OS.g_free (ptr [0]);
			}
		}
	}
	if (customDraw) {
		if (!ownerDraw) {
			ptr [0] = 0;
			OS.gtk_tree_model_get (tree_model, iter, modelIndex + CELL_BACKGROUND, ptr, -1);
			if (ptr [0] != 0) {
				OS.g_object_set (cell, OS.cell_background_gdk, ptr [0], 0);
				OS.gdk_color_free (ptr [0]);
			}
		}
		if (!isPixbuf) {
			ptr [0] = 0;
			OS.gtk_tree_model_get (tree_model, iter, modelIndex + CELL_FOREGROUND, ptr, -1);
			if (ptr [0] != 0) {
				OS.g_object_set (cell, OS.foreground_gdk, ptr [0], 0);
				OS.gdk_color_free (ptr [0]);
			}
			ptr [0] = 0;
			OS.gtk_tree_model_get (tree_model, iter, modelIndex + CELL_FONT, ptr, -1);
			if (ptr [0] != 0) {
				OS.g_object_set (cell, OS.font_desc, ptr [0], 0);
				OS.pango_font_description_free (ptr [0]);
			}
		}
	}
	if (setData) {
		ignoreCell = cell;
		setScrollWidth (tree_column, item);
		ignoreCell = 0;
	}
	return 0;
}

boolean checkData (TableItem item) {
	if (item.cached) return true;
	if ((style & SWT.VIRTUAL) != 0) {
		item.cached = true;
		Event event = new Event ();
		event.item = item;
		event.index = indexOf (item);
		int mask = OS.G_SIGNAL_MATCH_DATA | OS.G_SIGNAL_MATCH_ID;
		int signal_id = OS.g_signal_lookup (OS.row_changed, OS.gtk_tree_model_get_type ());
		OS.g_signal_handlers_block_matched (modelHandle, mask, signal_id, 0, 0, 0, handle);
		currentItem = item;
		sendEvent (SWT.SetData, event);
		//widget could be disposed at this point
		currentItem = null;
		if (isDisposed ()) return false;
		OS.g_signal_handlers_unblock_matched (modelHandle, mask, signal_id, 0, 0, 0, handle);
		if (item.isDisposed ()) return false;
	}
	return true;
}

@Override
protected void checkSubclass () {
	if (!isValidSubclass ()) error (SWT.ERROR_INVALID_SUBCLASS);
}

/**
 * Adds the listener to the collection of listeners who will
 * be notified when the user changes the receiver's selection, by sending
 * it one of the messages defined in the <code>SelectionListener</code>
 * interface.
 * <p>
 * When <code>widgetSelected</code> is called, the item field of the event object is valid.
 * If the receiver has the <code>SWT.CHECK</code> style and the check selection changes,
 * the event object detail field contains the value <code>SWT.CHECK</code>.
 * <code>widgetDefaultSelected</code> is typically called when an item is double-clicked.
 * The item field of the event object is valid for default selection, but the detail field is not used.
 * </p>
 *
 * @param listener the listener which should be notified when the user changes the receiver's selection
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see SelectionListener
 * @see #removeSelectionListener
 * @see SelectionEvent
 */
public void addSelectionListener (SelectionListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Selection, typedListener);
	addListener (SWT.DefaultSelection, typedListener);
}

int calculateWidth (long /*int*/ column, long /*int*/ iter) {
	OS.gtk_tree_view_column_cell_set_cell_data (column, modelHandle, iter, false, false);

	/*
	* Bug in GTK.  The width calculated by gtk_tree_view_column_cell_get_size()
	* always grows in size regardless of the text or images in the table.
	* The fix is to determine the column width from the cell renderers.
	*/

	//This workaround is causing the problem Bug 459834 in GTK3. So reverting the workaround for GTK3
	if (OS.GTK3) {
		int [] width = new int [1];
		OS.gtk_tree_view_column_cell_get_size (column, null, null, null, width, null);
		return width [0];
	} else {
		int width = 0;
		int [] w = new int [1];
		OS.gtk_widget_style_get(handle, OS.focus_line_width, w, 0);
		width += 2 * w [0];
		long /*int*/ list = OS.gtk_cell_layout_get_cells(column);
		if (list == 0) return 0;
		long /*int*/ temp = list;
		while (temp != 0) {
			long /*int*/ renderer = OS.g_list_data (temp);
			if (renderer != 0) {
				gtk_cell_renderer_get_preferred_size (renderer, handle, w, null);
				width += w [0];
			}
			temp = OS.g_list_next (temp);
		}
		OS.g_list_free (list);
		if (OS.gtk_tree_view_get_grid_lines(handle) > OS.GTK_TREE_VIEW_GRID_LINES_NONE) {
			OS.gtk_widget_style_get (handle, OS.grid_line_width, w, 0) ;
			width += 2 * w [0];
		}
		return width;
	}
}

/**
 * Clears the item at the given zero-relative index in the receiver.
 * The text, icon and other attributes of the item are set to the default
 * value.  If the table was created with the <code>SWT.VIRTUAL</code> style,
 * these attributes are requested again as needed.
 *
 * @param index the index of the item to clear
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see SWT#VIRTUAL
 * @see SWT#SetData
 *
 * @since 3.0
 */
public void clear (int index) {
	checkWidget ();
	if (!(0 <= index && index < itemCount)) {
		error(SWT.ERROR_INVALID_RANGE);
	}
	TableItem item = items [index];
	if (item != null) item.clear ();
}

/**
 * Removes the items from the receiver which are between the given
 * zero-relative start and end indices (inclusive).  The text, icon
 * and other attributes of the items are set to their default values.
 * If the table was created with the <code>SWT.VIRTUAL</code> style,
 * these attributes are requested again as needed.
 *
 * @param start the start index of the item to clear
 * @param end the end index of the item to clear
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_RANGE - if either the start or end are not between 0 and the number of elements in the list minus 1 (inclusive)</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see SWT#VIRTUAL
 * @see SWT#SetData
 *
 * @since 3.0
 */
public void clear (int start, int end) {
	checkWidget ();
	if (start > end) return;
	if (!(0 <= start && start <= end && end < itemCount)) {
		error (SWT.ERROR_INVALID_RANGE);
	}
	if (start == 0 && end == itemCount - 1) {
		clearAll();
	} else {
		for (int i=start; i<=end; i++) {
			TableItem item = items [i];
			if (item != null) item.clear();
		}
	}
}

/**
 * Clears the items at the given zero-relative indices in the receiver.
 * The text, icon and other attributes of the items are set to their default
 * values.  If the table was created with the <code>SWT.VIRTUAL</code> style,
 * these attributes are requested again as needed.
 *
 * @param indices the array of indices of the items
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
 *    <li>ERROR_NULL_ARGUMENT - if the indices array is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see SWT#VIRTUAL
 * @see SWT#SetData
 *
 * @since 3.0
 */
public void clear (int [] indices) {
	checkWidget ();
	if (indices == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (indices.length == 0) return;
	for (int i=0; i<indices.length; i++) {
		if (!(0 <= indices [i] && indices [i] < itemCount)) {
			error (SWT.ERROR_INVALID_RANGE);
		}
	}
	for (int i=0; i<indices.length; i++) {
		TableItem item = items [indices [i]];
		if (item != null) item.clear();
	}
}

/**
 * Clears all the items in the receiver. The text, icon and other
 * attributes of the items are set to their default values. If the
 * table was created with the <code>SWT.VIRTUAL</code> style, these
 * attributes are requested again as needed.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see SWT#VIRTUAL
 * @see SWT#SetData
 *
 * @since 3.0
 */
public void clearAll () {
	checkWidget ();
	for (int i=0; i<itemCount; i++) {
		TableItem item = items [i];
		if (item != null) item.clear();
	}
}

@Override Point computeSizeInPixels (int wHint, int hHint, boolean changed) {
	checkWidget ();
	if (wHint != SWT.DEFAULT && wHint < 0) wHint = 0;
	if (hHint != SWT.DEFAULT && hHint < 0) hHint = 0;
	Point size = computeNativeSize (handle, wHint, hHint, changed);
	if (size.x == 0 && wHint == SWT.DEFAULT) size.x = DEFAULT_WIDTH;

	/*
	 * in GTK 3.8 computeNativeSize returning 0 for height.
	 * So if the height is returned as zero calculate the table height
	 * based on the number of items in the table
	 */
	if (OS.GTK3 && size.y == 0 && hHint == SWT.DEFAULT) {
		size.y = getItemCount() * getItemHeightInPixels();
	}

	/*
	 * In case the table doesn't contain any elements,
	 * getItemCount returns 0 and size.y will be 0
	 * so need to assign default height
	 */
	if (size.y == 0 && hHint == SWT.DEFAULT) size.y = DEFAULT_HEIGHT;
	Rectangle trim = computeTrimInPixels (0, 0, size.x, size.y);
	size.x = trim.width;
	size.y = trim.height;
	return size;
}

void createColumn (TableColumn column, int index) {
	int modelIndex = FIRST_COLUMN;
	if (columnCount != 0) {
		int modelLength = OS.gtk_tree_model_get_n_columns (modelHandle);
		boolean [] usedColumns = new boolean [modelLength];
		for (int i=0; i<columnCount; i++) {
			int columnIndex = columns [i].modelIndex;
			for (int j = 0; j < CELL_TYPES; j++) {
				usedColumns [columnIndex + j] = true;
			}
		}
		while (modelIndex < modelLength) {
			if (!usedColumns [modelIndex]) break;
			modelIndex++;
		}
		if (modelIndex == modelLength) {
			long /*int*/ oldModel = modelHandle;
			long /*int*/[] types = getColumnTypes (columnCount + 4); // grow by 4 rows at a time
			long /*int*/ newModel = OS.gtk_list_store_newv (types.length, types);
			if (newModel == 0) error (SWT.ERROR_NO_HANDLES);
			long /*int*/ [] ptr = new long /*int*/ [1];
			int [] ptr1 = new int [1];
			for (int i=0; i<itemCount; i++) {
				long /*int*/ newItem = OS.g_malloc (OS.GtkTreeIter_sizeof ());
				if (newItem == 0) error (SWT.ERROR_NO_HANDLES);
				OS.gtk_list_store_append (newModel, newItem);
				TableItem item = items [i];
				if (item != null) {
					long /*int*/ oldItem = item.handle;
					/* the columns before FOREGROUND_COLUMN contain int values, subsequent columns contain pointers */
					for (int j=0; j<FOREGROUND_COLUMN; j++) {
						OS.gtk_tree_model_get (oldModel, oldItem, j, ptr1, -1);
						OS.gtk_list_store_set (newModel, newItem, j, ptr1 [0], -1);
					}
					for (int j=FOREGROUND_COLUMN; j<modelLength; j++) {
						OS.gtk_tree_model_get (oldModel, oldItem, j, ptr, -1);
						OS.gtk_list_store_set (newModel, newItem, j, ptr [0], -1);
						if (ptr [0] != 0) {
							if (types [j] == OS.G_TYPE_STRING ()) {
								OS.g_free ((ptr [0]));
							} else if (types [j] == OS.GDK_TYPE_COLOR()) {
								OS.gdk_color_free (ptr [0]);
							} else if (types [j] == OS.GDK_TYPE_PIXBUF()) {
								OS.g_object_unref (ptr [0]);
							} else if (types [j] == OS.PANGO_TYPE_FONT_DESCRIPTION()) {
								OS.pango_font_description_free (ptr [0]);
							}
						}
					}
					OS.gtk_list_store_remove (oldModel, oldItem);
					OS.g_free (oldItem);
					item.handle = newItem;
				} else {
					OS.g_free (newItem);
				}
			}
			OS.gtk_tree_view_set_model (handle, newModel);
			setModel (newModel);
		}
	}
	long /*int*/ columnHandle = OS.gtk_tree_view_column_new ();
	if (columnHandle == 0) error (SWT.ERROR_NO_HANDLES);
	if (index == 0 && columnCount > 0) {
		TableColumn checkColumn = columns [0];
		createRenderers (checkColumn.handle, checkColumn.modelIndex, false, checkColumn.style);
	}
	createRenderers (columnHandle, modelIndex, index == 0, column == null ? 0 : column.style);
	if ((style & SWT.VIRTUAL) == 0 && columnCount == 0) {
		OS.gtk_tree_view_column_set_sizing (columnHandle, OS.GTK_TREE_VIEW_COLUMN_GROW_ONLY);
	} else {
		OS.gtk_tree_view_column_set_sizing (columnHandle, OS.GTK_TREE_VIEW_COLUMN_FIXED);
	}
	OS.gtk_tree_view_column_set_resizable (columnHandle, true);
	OS.gtk_tree_view_column_set_clickable (columnHandle, true);
	OS.gtk_tree_view_column_set_min_width (columnHandle, 0);
	OS.gtk_tree_view_insert_column (handle, columnHandle, index);
	/*
	* Bug in GTK3.  The column header has the wrong CSS styling if it is hidden
	* when inserting to the tree widget.  The fix is to hide the column only
	* after it is inserted.
	*/
	if (columnCount != 0) OS.gtk_tree_view_column_set_visible (columnHandle, false);
	if (column != null) {
		column.handle = columnHandle;
		column.modelIndex = modelIndex;
	}
	if (!searchEnabled ()) {
		OS.gtk_tree_view_set_search_column (handle, -1);
	} else {
		/* Set the search column whenever the model changes */
		int firstColumn = columnCount == 0 ? FIRST_COLUMN : columns [0].modelIndex;
		OS.gtk_tree_view_set_search_column (handle, firstColumn + CELL_TEXT);
	}
}

@Override
void createHandle (int index) {
	state |= HANDLE;
	fixedHandle = OS.g_object_new (display.gtk_fixed_get_type (), 0);
	if (fixedHandle == 0) error (SWT.ERROR_NO_HANDLES);
	OS.gtk_widget_set_has_window (fixedHandle, true);
	scrolledHandle = OS.gtk_scrolled_window_new (0, 0);
	if (scrolledHandle == 0) error (SWT.ERROR_NO_HANDLES);
	long /*int*/ [] types = getColumnTypes (1);
	modelHandle = OS.gtk_list_store_newv (types.length, types);
	if (modelHandle == 0) error (SWT.ERROR_NO_HANDLES);
	handle = OS.gtk_tree_view_new_with_model (modelHandle);
	if (handle == 0) error (SWT.ERROR_NO_HANDLES);
	if ((style & SWT.CHECK) != 0) {
		checkRenderer = OS.gtk_cell_renderer_toggle_new ();
		if (checkRenderer == 0) error (SWT.ERROR_NO_HANDLES);
		OS.g_object_ref (checkRenderer);
	}
	createColumn (null, 0);
	OS.gtk_container_add (fixedHandle, scrolledHandle);
	OS.gtk_container_add (scrolledHandle, handle);

	int mode = (style & SWT.MULTI) != 0 ? OS.GTK_SELECTION_MULTIPLE : OS.GTK_SELECTION_BROWSE;
	long /*int*/ selectionHandle = OS.gtk_tree_view_get_selection (handle);
	OS.gtk_tree_selection_set_mode (selectionHandle, mode);
	OS.gtk_tree_view_set_headers_visible (handle, false);
	int hsp = (style & SWT.H_SCROLL) != 0 ? OS.GTK_POLICY_AUTOMATIC : OS.GTK_POLICY_NEVER;
	int vsp = (style & SWT.V_SCROLL) != 0 ? OS.GTK_POLICY_AUTOMATIC : OS.GTK_POLICY_NEVER;
	OS.gtk_scrolled_window_set_policy (scrolledHandle, hsp, vsp);
	if ((style & SWT.BORDER) != 0) OS.gtk_scrolled_window_set_shadow_type (scrolledHandle, OS.GTK_SHADOW_ETCHED_IN);
	/*
	 * Feature in GTK3: because of Bug 469277 & 476419, we now size Table icons dynamically, giving them an
	 * initial size of 0x0 until an image is added. We need to disable fixed_height_mode in GTK3 to enable
	 * this dynamic sizing behavior.
	 */
	if ((style & SWT.VIRTUAL) != 0 && !OS.GTK3) {
		OS.g_object_set (handle, OS.fixed_height_mode, true, 0);
	}
	if (!searchEnabled ()) {
		OS.gtk_tree_view_set_search_column (handle, -1);
	}
}

void createItem (TableColumn column, int index) {
	if (!(0 <= index && index <= columnCount)) error (SWT.ERROR_INVALID_RANGE);
	if (columnCount == 0) {
		column.handle = OS.gtk_tree_view_get_column (handle, 0);
		OS.gtk_tree_view_column_set_sizing (column.handle, OS.GTK_TREE_VIEW_COLUMN_FIXED);
		OS.gtk_tree_view_column_set_visible (column.handle, false);
		column.modelIndex = FIRST_COLUMN;
		createRenderers (column.handle, column.modelIndex, true, column.style);
		column.customDraw = firstCustomDraw;
		firstCustomDraw = false;
	} else {
		createColumn (column, index);
	}
	long /*int*/ boxHandle = gtk_box_new (OS.GTK_ORIENTATION_HORIZONTAL, false, 3);
	if (boxHandle == 0) error (SWT.ERROR_NO_HANDLES);
	long /*int*/ labelHandle = OS.gtk_label_new_with_mnemonic (null);
	if (labelHandle == 0) error (SWT.ERROR_NO_HANDLES);
	long /*int*/ imageHandle = OS.gtk_image_new ();
	if (imageHandle == 0) error (SWT.ERROR_NO_HANDLES);
	OS.gtk_container_add (boxHandle, imageHandle);
	OS.gtk_container_add (boxHandle, labelHandle);
	OS.gtk_widget_show (boxHandle);
	OS.gtk_widget_show (labelHandle);
	column.labelHandle = labelHandle;
	column.imageHandle = imageHandle;
	OS.gtk_tree_view_column_set_widget (column.handle, boxHandle);
	if (OS.GTK3) {
		column.buttonHandle = OS.gtk_tree_view_column_get_button(column.handle);
	} else {
		long /*int*/ widget = OS.gtk_widget_get_parent (boxHandle);
		while (widget != handle) {
			if (OS.GTK_IS_BUTTON (widget)) {
				column.buttonHandle = widget;
				break;
			}
			widget = OS.gtk_widget_get_parent (widget);
		}
	}
	if (columnCount == columns.length) {
		TableColumn [] newColumns = new TableColumn [columns.length + 4];
		System.arraycopy (columns, 0, newColumns, 0, columns.length);
		columns = newColumns;
	}
	System.arraycopy (columns, index, columns, index + 1, columnCount++ - index);
	columns [index] = column;
	if ((state & FONT) != 0) {
		column.setFontDescription (getFontDescription ());
	}
	if (columnCount >= 1) {
		for (int i=0; i<itemCount; i++) {
			TableItem item = items [i];
			if (item != null) {
				Font [] cellFont = item.cellFont;
				if (cellFont != null) {
					Font [] temp = new Font [columnCount];
					System.arraycopy (cellFont, 0, temp, 0, index);
					System.arraycopy (cellFont, index, temp, index+1, columnCount-index-1);
					item.cellFont = temp;
				}
			}
		}
	}
	/*
	 * Feature in GTK. The tree view does not resize immediately if a table
	 * column is created when the table is not visible. If the width of the
 	 * new column is queried, GTK returns an incorrect value. The fix is to
 	 * ensure that the columns are resized before any queries.
	 */
	if(!isVisible ()) {
		forceResize();
	}
}

void createItem (TableItem item, int index) {
	if (!(0 <= index && index <= itemCount)) error (SWT.ERROR_INVALID_RANGE);
	if (itemCount == items.length) {
		int length = drawCount <= 0 ? items.length + 4 : Math.max (4, items.length * 3 / 2);
		TableItem [] newItems = new TableItem [length];
		System.arraycopy (items, 0, newItems, 0, items.length);
		items = newItems;
	}
	item.handle = OS.g_malloc (OS.GtkTreeIter_sizeof ());
	if (item.handle == 0) error (SWT.ERROR_NO_HANDLES);
	/*
	* Feature in GTK.  It is much faster to append to a list store
	* than to insert at the end using gtk_list_store_insert().
	*/
	if (index == itemCount) {
		OS.gtk_list_store_append (modelHandle, item.handle);
	} else {
		OS.gtk_list_store_insert (modelHandle, item.handle, index);
	}
	System.arraycopy (items, index, items, index + 1, itemCount++ - index);
	items [index] = item;
}

void createRenderers (long /*int*/ columnHandle, int modelIndex, boolean check, int columnStyle) {
	OS.gtk_tree_view_column_clear (columnHandle);
	if ((style & SWT.CHECK) != 0 && check) {
		OS.gtk_tree_view_column_pack_start (columnHandle, checkRenderer, false);
		OS.gtk_tree_view_column_add_attribute (columnHandle, checkRenderer, OS.active, CHECKED_COLUMN);
		OS.gtk_tree_view_column_add_attribute (columnHandle, checkRenderer, OS.inconsistent, GRAYED_COLUMN);
		if (!ownerDraw) OS.gtk_tree_view_column_add_attribute (columnHandle, checkRenderer, OS.cell_background_gdk, BACKGROUND_COLUMN);
		if (ownerDraw) {
			OS.gtk_tree_view_column_set_cell_data_func (columnHandle, checkRenderer, display.cellDataProc, handle, 0);
			OS.g_object_set_qdata (checkRenderer, Display.SWT_OBJECT_INDEX1, columnHandle);
		}
	}
	long /*int*/ pixbufRenderer = ownerDraw ? OS.g_object_new (display.gtk_cell_renderer_pixbuf_get_type (), 0) : OS.gtk_cell_renderer_pixbuf_new ();
	if (pixbufRenderer == 0) {
		error (SWT.ERROR_NO_HANDLES);
	} else {
		// set default size this size is used for calculating the icon and text positions in a table
		if ((!ownerDraw) && (OS.GTK3)) {
			// Set render size to 0x0 until we actually add images, fix for
			// Bug 457196 (this applies to Tables as well).
			OS.gtk_cell_renderer_set_fixed_size(pixbufRenderer, 0, 0);
		}
	}
	long /*int*/ textRenderer = ownerDraw ? OS.g_object_new (display.gtk_cell_renderer_text_get_type (), 0) : OS.gtk_cell_renderer_text_new ();
	if (textRenderer == 0) error (SWT.ERROR_NO_HANDLES);

	if (ownerDraw) {
		OS.g_object_set_qdata (pixbufRenderer, Display.SWT_OBJECT_INDEX1, columnHandle);
		OS.g_object_set_qdata (textRenderer, Display.SWT_OBJECT_INDEX1, columnHandle);
	}

	/*
	* Feature in GTK.  When a tree view column contains only one activatable
	* cell renderer such as a toggle renderer, mouse clicks anywhere in a cell
	* activate that renderer. The workaround is to set a second cell renderer
	* to be activatable.
	*/
	if ((style & SWT.CHECK) != 0 && check) {
		OS.g_object_set (pixbufRenderer, OS.mode, OS.GTK_CELL_RENDERER_MODE_ACTIVATABLE, 0);
	}

	/* Set alignment */
	if ((columnStyle & SWT.RIGHT) != 0) {
		OS.g_object_set(textRenderer, OS.xalign, 1f, 0);
		OS.gtk_tree_view_column_pack_end (columnHandle, textRenderer, true);
		OS.gtk_tree_view_column_pack_end (columnHandle, pixbufRenderer, false);
		OS.gtk_tree_view_column_set_alignment (columnHandle, 1f);
	} else if ((columnStyle & SWT.CENTER) != 0) {
		OS.g_object_set(textRenderer, OS.xalign, 0.5f, 0);
		OS.gtk_tree_view_column_pack_start (columnHandle, pixbufRenderer, false);
		OS.gtk_tree_view_column_pack_end (columnHandle, textRenderer, true);
		OS.gtk_tree_view_column_set_alignment (columnHandle, 0.5f);
	} else {
		OS.gtk_tree_view_column_pack_start (columnHandle, pixbufRenderer, false);
		OS.gtk_tree_view_column_pack_start (columnHandle, textRenderer, true);
		OS.gtk_tree_view_column_set_alignment (columnHandle, 0f);
	}

	/* Add attributes */
	/*
	 * Formerly OS.gicon was set if on GTK3, but this is being removed do to spacing issues in Tables/Trees with
	 * no images. Fix for Bug 457196. NOTE: this change has been ported to Tables since Tables/Trees both
	 * use the same underlying GTK structure.
	 */
	OS.gtk_tree_view_column_add_attribute (columnHandle, pixbufRenderer, OS.pixbuf, modelIndex + CELL_PIXBUF);
	if (!ownerDraw) {
		OS.gtk_tree_view_column_add_attribute (columnHandle, pixbufRenderer, OS.cell_background_gdk, BACKGROUND_COLUMN);
		OS.gtk_tree_view_column_add_attribute (columnHandle, textRenderer, OS.cell_background_gdk, BACKGROUND_COLUMN);
	}
	OS.gtk_tree_view_column_add_attribute (columnHandle, textRenderer, OS.text, modelIndex + CELL_TEXT);
	OS.gtk_tree_view_column_add_attribute (columnHandle, textRenderer, OS.foreground_gdk, FOREGROUND_COLUMN);
	OS.gtk_tree_view_column_add_attribute (columnHandle, textRenderer, OS.font_desc, FONT_COLUMN);

	boolean customDraw = firstCustomDraw;
	if (columnCount != 0) {
		for (int i=0; i<columnCount; i++) {
			if (columns [i].handle == columnHandle) {
				customDraw = columns [i].customDraw;
				break;
			}
		}
	}
	if ((style & SWT.VIRTUAL) != 0 || customDraw || ownerDraw) {
		OS.gtk_tree_view_column_set_cell_data_func (columnHandle, textRenderer, display.cellDataProc, handle, 0);
		OS.gtk_tree_view_column_set_cell_data_func (columnHandle, pixbufRenderer, display.cellDataProc, handle, 0);
	}
}

@Override
void createWidget (int index) {
	super.createWidget (index);
	items = new TableItem [4];
	columns = new TableColumn [4];
	itemCount = columnCount = 0;
	// In GTK 3 font description is inherited from parent widget which is not how SWT has always worked,
	// reset to default font to get the usual behavior
	if (OS.GTK3) {
		setFontDescription(defaultFont().handle);
	}
}

@Override
int applyThemeBackground () {
	return -1; /* No Change */
}

GdkColor defaultBackground () {
	return display.COLOR_LIST_BACKGROUND;
}

GdkColor defaultForeground () {
	return display.COLOR_LIST_FOREGROUND;
}

@Override
void deregister () {
	super.deregister ();
	display.removeWidget (OS.gtk_tree_view_get_selection (handle));
	if (checkRenderer != 0) display.removeWidget (checkRenderer);
	display.removeWidget (modelHandle);
}

/**
 * Deselects the item at the given zero-relative index in the receiver.
 * If the item at the index was already deselected, it remains
 * deselected. Indices that are out of range are ignored.
 *
 * @param index the index of the item to deselect
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void deselect (int index) {
	checkWidget();
	if (index < 0 || index >= itemCount) return;
	boolean fixColumn = showFirstColumn ();
	long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
	OS.g_signal_handlers_block_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
	OS.gtk_tree_selection_unselect_iter (selection, _getItem (index).handle);
	OS.g_signal_handlers_unblock_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
	if (fixColumn) hideFirstColumn ();
}

/**
 * Deselects the items at the given zero-relative indices in the receiver.
 * If the item at the given zero-relative index in the receiver
 * is selected, it is deselected.  If the item at the index
 * was not selected, it remains deselected.  The range of the
 * indices is inclusive. Indices that are out of range are ignored.
 *
 * @param start the start index of the items to deselect
 * @param end the end index of the items to deselect
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void deselect (int start, int end) {
	checkWidget();
	boolean fixColumn = showFirstColumn ();
	long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
	OS.g_signal_handlers_block_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
	for (int index=start; index<=end; index++) {
		if (index < 0 || index >= itemCount) continue;
		OS.gtk_tree_selection_unselect_iter (selection, _getItem (index).handle);
	}
	OS.g_signal_handlers_unblock_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
	if (fixColumn) hideFirstColumn ();
}

/**
 * Deselects the items at the given zero-relative indices in the receiver.
 * If the item at the given zero-relative index in the receiver
 * is selected, it is deselected.  If the item at the index
 * was not selected, it remains deselected. Indices that are out
 * of range and duplicate indices are ignored.
 *
 * @param indices the array of indices for the items to deselect
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the set of indices is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void deselect (int [] indices) {
	checkWidget();
	if (indices == null) error (SWT.ERROR_NULL_ARGUMENT);
	boolean fixColumn = showFirstColumn ();
	long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
	OS.g_signal_handlers_block_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
	for (int i=0; i<indices.length; i++) {
		int index = indices[i];
		if (index < 0 || index >= itemCount) continue;
		OS.gtk_tree_selection_unselect_iter (selection, _getItem (index).handle);
	}
	OS.g_signal_handlers_unblock_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
	if (fixColumn) hideFirstColumn ();
}

/**
 * Deselects all selected items in the receiver.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void deselectAll () {
	checkWidget();
	boolean fixColumn = showFirstColumn ();
	long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
	OS.g_signal_handlers_block_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
	OS.gtk_tree_selection_unselect_all (selection);
	OS.g_signal_handlers_unblock_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
	if (fixColumn) hideFirstColumn ();
}

void destroyItem (TableColumn column) {
	int index = 0;
	while (index < columnCount) {
		if (columns [index] == column) break;
		index++;
	}
	if (index == columnCount) return;
	long /*int*/ columnHandle = column.handle;
	if (columnCount == 1) {
		firstCustomDraw = column.customDraw;
	}
	System.arraycopy (columns, index + 1, columns, index, --columnCount - index);
	columns [columnCount] = null;
	OS.gtk_tree_view_remove_column (handle, columnHandle);
	if (columnCount == 0) {
		long /*int*/ oldModel = modelHandle;
		long /*int*/[] types = getColumnTypes (1);
		long /*int*/ newModel = OS.gtk_list_store_newv (types.length, types);
		if (newModel == 0) error (SWT.ERROR_NO_HANDLES);
		long /*int*/ [] ptr = new long /*int*/ [1];
		int [] ptr1 = new int [1];
		for (int i=0; i<itemCount; i++) {
			long /*int*/ newItem = OS.g_malloc (OS.GtkTreeIter_sizeof ());
			if (newItem == 0) error (SWT.ERROR_NO_HANDLES);
			OS.gtk_list_store_append (newModel, newItem);
			TableItem item = items [i];
			if (item != null) {
				long /*int*/ oldItem = item.handle;
				/* the columns before FOREGROUND_COLUMN contain int values, subsequent columns contain pointers */
				for (int j=0; j<FOREGROUND_COLUMN; j++) {
					OS.gtk_tree_model_get (oldModel, oldItem, j, ptr1, -1);
					OS.gtk_list_store_set (newModel, newItem, j, ptr1 [0], -1);
				}
				for (int j=FOREGROUND_COLUMN; j<FIRST_COLUMN; j++) {
					OS.gtk_tree_model_get (oldModel, oldItem, j, ptr, -1);
					OS.gtk_list_store_set (newModel, newItem, j, ptr [0], -1);
					if (ptr [0] != 0) {
						if (j == FOREGROUND_COLUMN || j == BACKGROUND_COLUMN) {
							OS.gdk_color_free (ptr [0]);
						} else if (j == FONT_COLUMN) {
							OS.pango_font_description_free (ptr [0]);
						}
					}
				}
				OS.gtk_tree_model_get (oldModel, oldItem, column.modelIndex + CELL_PIXBUF, ptr, -1);
				OS.gtk_list_store_set (newModel, newItem, FIRST_COLUMN + CELL_PIXBUF, ptr [0], -1);
				if (ptr [0] != 0) OS.g_object_unref (ptr [0]);
				OS.gtk_tree_model_get (oldModel, oldItem, column.modelIndex + CELL_TEXT, ptr, -1);
				OS.gtk_list_store_set (newModel, newItem, FIRST_COLUMN + CELL_TEXT, ptr [0], -1);
				OS.g_free (ptr [0]);
				OS.gtk_tree_model_get (oldModel, oldItem, column.modelIndex + CELL_FOREGROUND, ptr, -1);
				OS.gtk_list_store_set (newModel, newItem, FIRST_COLUMN + CELL_FOREGROUND, ptr [0], -1);
				if (ptr [0] != 0) OS.gdk_color_free (ptr [0]);
				OS.gtk_tree_model_get (oldModel, oldItem, column.modelIndex + CELL_BACKGROUND, ptr, -1);
				OS.gtk_list_store_set (newModel, newItem, FIRST_COLUMN + CELL_BACKGROUND, ptr [0], -1);
				if (ptr [0] != 0) OS.gdk_color_free (ptr [0]);
				OS.gtk_tree_model_get (oldModel, oldItem, column.modelIndex + CELL_FONT, ptr, -1);
				OS.gtk_list_store_set (newModel, newItem, FIRST_COLUMN + CELL_FONT, ptr [0], -1);
				if (ptr [0] != 0) OS.pango_font_description_free (ptr [0]);
				OS.gtk_list_store_remove (oldModel, oldItem);
				OS.g_free (oldItem);
				item.handle = newItem;
			} else {
				OS.g_free (newItem);
			}
		}
		OS.gtk_tree_view_set_model (handle, newModel);
		setModel (newModel);
		createColumn (null, 0);
	} else {
		for (int i=0; i<itemCount; i++) {
			TableItem item = items [i];
			if (item != null) {
				long /*int*/ iter = item.handle;
				int modelIndex = column.modelIndex;
				OS.gtk_list_store_set (modelHandle, iter, modelIndex + CELL_PIXBUF, (long /*int*/)0, -1);
				OS.gtk_list_store_set (modelHandle, iter, modelIndex + CELL_TEXT, (long /*int*/)0, -1);
				OS.gtk_list_store_set (modelHandle, iter, modelIndex + CELL_FOREGROUND, (long /*int*/)0, -1);
				OS.gtk_list_store_set (modelHandle, iter, modelIndex + CELL_BACKGROUND, (long /*int*/)0, -1);
				OS.gtk_list_store_set (modelHandle, iter, modelIndex + CELL_FONT, (long /*int*/)0, -1);

				Font [] cellFont = item.cellFont;
				if (cellFont != null) {
					if (columnCount == 0) {
						item.cellFont = null;
					} else {
						Font [] temp = new Font [columnCount];
						System.arraycopy (cellFont, 0, temp, 0, index);
						System.arraycopy (cellFont, index + 1, temp, index, columnCount - index);
						item.cellFont = temp;
					}
				}
			}
		}
		if (index == 0) {
			TableColumn checkColumn = columns [0];
			createRenderers (checkColumn.handle, checkColumn.modelIndex, true, checkColumn.style);
		}
	}
	if (!searchEnabled ()) {
		OS.gtk_tree_view_set_search_column (handle, -1);
	} else {
		/* Set the search column whenever the model changes */
		int firstColumn = columnCount == 0 ? FIRST_COLUMN : columns [0].modelIndex;
		OS.gtk_tree_view_set_search_column (handle, firstColumn + CELL_TEXT);
	}
}

void destroyItem (TableItem item) {
	int index = 0;
	while (index < itemCount) {
		if (items [index] == item) break;
		index++;
	}
	if (index == itemCount) return;
	long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
	OS.g_signal_handlers_block_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
	OS.gtk_list_store_remove (modelHandle, item.handle);
	OS.g_signal_handlers_unblock_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
	System.arraycopy (items, index + 1, items, index, --itemCount - index);
	items [itemCount] = null;
	if (itemCount == 0) resetCustomDraw ();
}

@Override
boolean dragDetect (int x, int y, boolean filter, boolean dragOnTimeout, boolean [] consume) {
	boolean selected = false;
	if (filter) {
		long /*int*/ [] path = new long /*int*/ [1];
		if (OS.gtk_tree_view_get_path_at_pos (handle, x, y, path, null, null, null)) {
			if (path [0] != 0) {
				long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
				if (OS.gtk_tree_selection_path_is_selected (selection, path [0])) selected = true;
				OS.gtk_tree_path_free (path [0]);
			}
		} else {
			return false;
		}
	}
	boolean dragDetect = super.dragDetect (x, y, filter, false, consume);
	if (dragDetect && selected && consume != null) consume [0] = true;
	return dragDetect;
}

@Override
long /*int*/ eventWindow () {
	return paintWindow ();
}

boolean fixAccessibility () {
	/*
	* Bug in GTK. With GTK 2.12, when assistive technologies is on, the time
	* it takes to add or remove several rows to the model is very long. This
	* happens because the accessible object asks each row for its data, including
	* the rows that are not visible. The the fix is to block the accessible object
	* from receiving row_added and row_removed signals and, at the end, send only
	* a notify signal with the "model" detail.
	*
	* Note: The test bellow has to be updated when the real problem is fixed in
	* the accessible object.
	*/
	return true;
}

@Override
void fixChildren (Shell newShell, Shell oldShell, Decorations newDecorations, Decorations oldDecorations, Menu [] menus) {
	super.fixChildren (newShell, oldShell, newDecorations, oldDecorations, menus);
	for (int i=0; i<columnCount; i++) {
		TableColumn column = columns [i];
		if (column.toolTipText != null) {
			column.setToolTipText(oldShell, null);
			column.setToolTipText(newShell, column.toolTipText);
		}
	}
}

@Override
GdkColor getBackgroundColor () {
	return getBaseColor ();
}

@Override Rectangle getClientAreaInPixels () {
	checkWidget ();
	forceResize ();
	OS.gtk_widget_realize (handle);
	long /*int*/ fixedWindow = gtk_widget_get_window (fixedHandle);
	long /*int*/ binWindow = OS.gtk_tree_view_get_bin_window (handle);
	int [] binX = new int [1], binY = new int [1];
	OS.gdk_window_get_origin (binWindow, binX, binY);
	int [] fixedX = new int [1], fixedY = new int [1];
	OS.gdk_window_get_origin (fixedWindow, fixedX, fixedY);
	long /*int*/ clientHandle = clientHandle ();
	GtkAllocation allocation = new GtkAllocation ();
	OS.gtk_widget_get_allocation (clientHandle, allocation);
	int width = (state & ZERO_WIDTH) != 0 ? 0 : allocation.width;
	int height = (state & ZERO_HEIGHT) != 0 ? 0 : allocation.height;
	Rectangle rect = new Rectangle (fixedX [0] - binX [0], fixedY [0] - binY [0], width, height);
	if (getHeaderVisible() && OS.GTK_VERSION > OS.VERSION(3, 9, 0)) {
		rect.y += getHeaderHeightInPixels();
	}
	return rect;
}

@Override
int getClientWidth () {
	int [] w = new int [1], h = new int [1];
	OS.gtk_widget_realize (handle);
	gdk_window_get_size(OS.gtk_tree_view_get_bin_window(handle), w, h);
	return w[0];
}

/**
 * Returns the column at the given, zero-relative index in the
 * receiver. Throws an exception if the index is out of range.
 * Columns are returned in the order that they were created.
 * If no <code>TableColumn</code>s were created by the programmer,
 * this method will throw <code>ERROR_INVALID_RANGE</code> despite
 * the fact that a single column of data may be visible in the table.
 * This occurs when the programmer uses the table like a list, adding
 * items but never creating a column.
 *
 * @param index the index of the column to return
 * @return the column at the given index
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see Table#getColumnOrder()
 * @see Table#setColumnOrder(int[])
 * @see TableColumn#getMoveable()
 * @see TableColumn#setMoveable(boolean)
 * @see SWT#Move
 */
public TableColumn getColumn (int index) {
	checkWidget();
	if (!(0 <= index && index < columnCount)) error (SWT.ERROR_INVALID_RANGE);
	return columns [index];
}

/**
 * Returns the number of columns contained in the receiver.
 * If no <code>TableColumn</code>s were created by the programmer,
 * this value is zero, despite the fact that visually, one column
 * of items may be visible. This occurs when the programmer uses
 * the table like a list, adding items but never creating a column.
 *
 * @return the number of columns
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getColumnCount () {
	checkWidget();
	return columnCount;
}

long /*int*/[] getColumnTypes (int columnCount) {
	long /*int*/[] types = new long /*int*/ [FIRST_COLUMN + (columnCount * CELL_TYPES)];
	// per row data
	types [CHECKED_COLUMN] = OS.G_TYPE_BOOLEAN ();
	types [GRAYED_COLUMN] = OS.G_TYPE_BOOLEAN ();
	types [FOREGROUND_COLUMN] = OS.GDK_TYPE_COLOR ();
	types [BACKGROUND_COLUMN] = OS.GDK_TYPE_COLOR ();
	types [FONT_COLUMN] = OS.PANGO_TYPE_FONT_DESCRIPTION ();
	// per cell data
	for (int i=FIRST_COLUMN; i<types.length; i+=CELL_TYPES) {
		types [i + CELL_PIXBUF] = OS.GDK_TYPE_PIXBUF ();
		types [i + CELL_TEXT] = OS.G_TYPE_STRING ();
		types [i + CELL_FOREGROUND] = OS.GDK_TYPE_COLOR ();
		types [i + CELL_BACKGROUND] = OS.GDK_TYPE_COLOR ();
		types [i + CELL_FONT] = OS.PANGO_TYPE_FONT_DESCRIPTION ();
	}
	return types;
}

/**
 * Returns an array of zero-relative integers that map
 * the creation order of the receiver's items to the
 * order in which they are currently being displayed.
 * <p>
 * Specifically, the indices of the returned array represent
 * the current visual order of the items, and the contents
 * of the array represent the creation order of the items.
 * </p><p>
 * Note: This is not the actual structure used by the receiver
 * to maintain its list of items, so modifying the array will
 * not affect the receiver.
 * </p>
 *
 * @return the current visual order of the receiver's items
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see Table#setColumnOrder(int[])
 * @see TableColumn#getMoveable()
 * @see TableColumn#setMoveable(boolean)
 * @see SWT#Move
 *
 * @since 3.1
 */
public int [] getColumnOrder () {
	checkWidget ();
	if (columnCount == 0) return new int [0];
	long /*int*/ list = OS.gtk_tree_view_get_columns (handle);
	if (list == 0) return new int [0];
	int  i = 0, count = OS.g_list_length (list);
	int [] order = new int [count];
	long /*int*/ temp = list;
	while (temp != 0) {
		long /*int*/ column = OS.g_list_data (temp);
		if (column != 0) {
			for (int j=0; j<columnCount; j++) {
				if (columns [j].handle == column) {
					order [i++] = j;
					break;
				}
			}
		}
		temp = OS.g_list_next (temp);
	}
	OS.g_list_free (list);
	return order;
}

/**
 * Returns an array of <code>TableColumn</code>s which are the
 * columns in the receiver.  Columns are returned in the order
 * that they were created.  If no <code>TableColumn</code>s were
 * created by the programmer, the array is empty, despite the fact
 * that visually, one column of items may be visible. This occurs
 * when the programmer uses the table like a list, adding items but
 * never creating a column.
 * <p>
 * Note: This is not the actual structure used by the receiver
 * to maintain its list of items, so modifying the array will
 * not affect the receiver.
 * </p>
 *
 * @return the items in the receiver
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see Table#getColumnOrder()
 * @see Table#setColumnOrder(int[])
 * @see TableColumn#getMoveable()
 * @see TableColumn#setMoveable(boolean)
 * @see SWT#Move
 */
public TableColumn [] getColumns () {
	checkWidget();
	TableColumn [] result = new TableColumn [columnCount];
	System.arraycopy (columns, 0, result, 0, columnCount);
	return result;
}

@Override
GdkColor getContextBackground () {
	if (OS.GTK_VERSION >= OS.VERSION(3, 16, 0)) {
		if (background != null) {
			return display.toGdkColor (background);
		} else {
			// For Tables and Trees, the default background is
			// COLOR_LIST_BACKGROUND instead of COLOR_WIDGET_BACKGROUND.
			return display.COLOR_LIST_BACKGROUND;
		}
	} else {
		return super.getContextBackground ();
	}
}

TableItem getFocusItem () {
	long /*int*/ [] path = new long /*int*/ [1];
	OS.gtk_tree_view_get_cursor (handle, path, null);
	if (path [0] == 0) return null;
	TableItem item = null;
	long /*int*/ indices = OS.gtk_tree_path_get_indices (path [0]);
	if (indices != 0) {
		int [] index = new int []{-1};
		OS.memmove (index, indices, 4);
		item = _getItem (index [0]);
	}
	OS.gtk_tree_path_free (path [0]);
	return item;
}

@Override
GdkColor getForegroundColor () {
	return getTextColor ();
}

/**
 * Returns the width in pixels of a grid line.
 *
 * @return the width of a grid line in pixels
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getGridLineWidth () {
	return DPIUtil.autoScaleDown (getGridLineWidthInPixels ());
}

/**
 * Returns the width in pixels of a grid line.
 *
 * @return the width of a grid line in pixels
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * @since 3.105
 */
int getGridLineWidthInPixels () {
	checkWidget();
	return 0;
}

/**
 * Returns the height of the receiver's header
 *
 * @return the height of the header or zero if the header is not visible
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 2.0
 */
public int getHeaderHeight () {
	return DPIUtil.autoScaleDown (getHeaderHeightInPixels ());
}

/**
 * Returns the height of the receiver's header
 *
 * @return the height of the header or zero if the header is not visible
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 3.105
 */
int getHeaderHeightInPixels () {
	checkWidget ();
	if (!OS.gtk_tree_view_get_headers_visible (handle)) return 0;
	if (columnCount > 0) {
		GtkRequisition requisition = new GtkRequisition ();
		int height = 0;
		for (int i=0; i<columnCount; i++) {
			long /*int*/ buttonHandle = columns [i].buttonHandle;
			if (buttonHandle != 0) {
				gtk_widget_get_preferred_size (buttonHandle, requisition);
				height = Math.max (height, requisition.height);
			}
		}
		return height;
	}
	OS.gtk_widget_realize (handle);
	long /*int*/ fixedWindow = gtk_widget_get_window (fixedHandle);
	long /*int*/ binWindow = OS.gtk_tree_view_get_bin_window (handle);
	int [] binY = new int [1];
	OS.gdk_window_get_origin (binWindow, null, binY);
	int [] fixedY = new int [1];
	OS.gdk_window_get_origin (fixedWindow, null, fixedY);
	return binY [0] - fixedY [0];
}

/**
 * Returns <code>true</code> if the receiver's header is visible,
 * and <code>false</code> otherwise.
 * <p>
 * If one of the receiver's ancestors is not visible or some
 * other condition makes the receiver not visible, this method
 * may still indicate that it is considered visible even though
 * it may not actually be showing.
 * </p>
 *
 * @return the receiver's header's visibility state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean getHeaderVisible () {
	checkWidget();
	return OS.gtk_tree_view_get_headers_visible (handle);
}

/**
 * Returns the item at the given, zero-relative index in the
 * receiver. Throws an exception if the index is out of range.
 *
 * @param index the index of the item to return
 * @return the item at the given index
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public TableItem getItem (int index) {
	checkWidget();
	if (!(0 <= index && index < itemCount)) error (SWT.ERROR_INVALID_RANGE);
	return _getItem (index);
}

/**
 * Returns the item at the given point in the receiver
 * or null if no such item exists. The point is in the
 * coordinate system of the receiver.
 * <p>
 * The item that is returned represents an item that could be selected by the user.
 * For example, if selection only occurs in items in the first column, then null is
 * returned if the point is outside of the item.
 * Note that the SWT.FULL_SELECTION style hint, which specifies the selection policy,
 * determines the extent of the selection.
 * </p>
 *
 * @param point the point used to locate the item
 * @return the item at the given point, or null if the point is not in a selectable item
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public TableItem getItem (Point point) {
	checkWidget();
	return getItemInPixels(DPIUtil.autoScaleUp(point));
}
/**
 * Returns the item at the given point in the receiver
 * or null if no such item exists. The point is in the
 * coordinate system of the receiver.
 * <p>
 * The item that is returned represents an item that could be selected by the user.
 * For example, if selection only occurs in items in the first column, then null is
 * returned if the point is outside of the item.
 * Note that the SWT.FULL_SELECTION style hint, which specifies the selection policy,
 * determines the extent of the selection.
 * </p>
 *
 * @param point the point used to locate the item
 * @return the item at the given point, or null if the point is not in a selectable item
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
TableItem getItemInPixels (Point point) {
	checkWidget();
	if (point == null) error (SWT.ERROR_NULL_ARGUMENT);
	long /*int*/ [] path = new long /*int*/ [1];
	OS.gtk_widget_realize (handle);
	int y = point.y;
	if (getHeaderVisible() && OS.GTK_VERSION > OS.VERSION(3, 9, 0)) {
		y -= getHeaderHeightInPixels();
	}
	if (!OS.gtk_tree_view_get_path_at_pos (handle, point.x, y, path, null, null, null)) return null;
	if (path [0] == 0) return null;
	long /*int*/ indices = OS.gtk_tree_path_get_indices (path [0]);
	TableItem item = null;
	if (indices != 0) {
		int [] index = new int [1];
		OS.memmove (index, indices, 4);
		item = _getItem (index [0]);
	}
	OS.gtk_tree_path_free (path [0]);
	return item;
}

/**
 * Returns the number of items contained in the receiver.
 *
 * @return the number of items
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getItemCount () {
	checkWidget ();
	return itemCount;
}

/**
 * Returns the height of the area which would be used to
 * display <em>one</em> of the items in the receiver.
 *
 * @return the height of one item
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getItemHeight () {
	return DPIUtil.autoScaleDown (getItemHeightInPixels ());
}

/**
 * Returns the height of the area which would be used to
 * display <em>one</em> of the items in the receiver.
 *
 * @return the height of one item
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * @since 3.105
 */
int getItemHeightInPixels () {
	checkWidget();
	if (itemCount == 0) {
		long /*int*/ column = OS.gtk_tree_view_get_column (handle, 0);
		int [] w = new int [1], h = new int [1];
		ignoreSize = true;
		OS.gtk_tree_view_column_cell_get_size (column, null, null, null, w, h);
		int height = h [0];
		if (OS.GTK3) {
			long /*int*/ textRenderer = getTextRenderer (column);
			OS.gtk_cell_renderer_get_preferred_height_for_width (textRenderer, handle, 0, h, null);
			height += h [0];
		}
		ignoreSize = false;
		return height;
	} else {
		int height = 0;
		long /*int*/ iter = OS.g_malloc (OS.GtkTreeIter_sizeof ());
		OS.gtk_tree_model_get_iter_first (modelHandle, iter);
		int columnCount = Math.max (1, this.columnCount);
		for (int i=0; i<columnCount; i++) {
			long /*int*/ column = OS.gtk_tree_view_get_column (handle, i);
			OS.gtk_tree_view_column_cell_set_cell_data (column, modelHandle, iter, false, false);
			int [] w = new int [1], h = new int [1];
			OS.gtk_tree_view_column_cell_get_size (column, null, null, null, w, h);
			height = Math.max (height, h [0]);
		}
		OS.g_free (iter);
		return height;
	}
}

/**
 * Returns a (possibly empty) array of <code>TableItem</code>s which
 * are the items in the receiver.
 * <p>
 * Note: This is not the actual structure used by the receiver
 * to maintain its list of items, so modifying the array will
 * not affect the receiver.
 * </p>
 *
 * @return the items in the receiver
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public TableItem [] getItems () {
	checkWidget();
	TableItem [] result = new TableItem [itemCount];
	if ((style & SWT.VIRTUAL) != 0) {
		for (int i=0; i<itemCount; i++) {
			result [i] = _getItem (i);
		}
	} else {
		System.arraycopy (items, 0, result, 0, itemCount);
	}
	return result;
}

/**
 * Returns <code>true</code> if the receiver's lines are visible,
 * and <code>false</code> otherwise. Note that some platforms draw
 * grid lines while others may draw alternating row colors.
 * <p>
 * If one of the receiver's ancestors is not visible or some
 * other condition makes the receiver not visible, this method
 * may still indicate that it is considered visible even though
 * it may not actually be showing.
 * </p>
 *
 * @return the visibility state of the lines
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean getLinesVisible() {
	checkWidget();
	if (OS.GTK3) {
		return OS.gtk_tree_view_get_grid_lines(handle) > OS.GTK_TREE_VIEW_GRID_LINES_NONE;
	} else  {
		return OS.gtk_tree_view_get_rules_hint (handle);
	}
}

long /*int*/ getPixbufRenderer (long /*int*/ column) {
	long /*int*/ list = OS.gtk_cell_layout_get_cells(column);
	if (list == 0) return 0;
	long /*int*/ originalList = list;
	long /*int*/ pixbufRenderer = 0;
	while (list != 0) {
		long /*int*/ renderer = OS.g_list_data (list);
		if (OS.GTK_IS_CELL_RENDERER_PIXBUF (renderer)) {
			pixbufRenderer = renderer;
			break;
		}
		list = OS.g_list_next (list);
	}
	OS.g_list_free (originalList);
	return pixbufRenderer;
}

/**
 * Returns an array of <code>TableItem</code>s that are currently
 * selected in the receiver. The order of the items is unspecified.
 * An empty array indicates that no items are selected.
 * <p>
 * Note: This is not the actual structure used by the receiver
 * to maintain its selection, so modifying the array will
 * not affect the receiver.
 * </p>
 * @return an array representing the selection
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public TableItem [] getSelection () {
	checkWidget();
	long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
	long /*int*/ list = OS.gtk_tree_selection_get_selected_rows (selection, null);
	long /*int*/ originalList = list;
	if (list != 0) {
		int count = OS.g_list_length (list);
		int [] treeSelection = new int [count];
		int length = 0;
		for (int i=0; i<count; i++) {
			long /*int*/ data = OS.g_list_data (list);
			long /*int*/ indices = OS.gtk_tree_path_get_indices (data);
			if (indices != 0) {
				int [] index = new int [1];
				OS.memmove (index, indices, 4);
				treeSelection [length] = index [0];
				length++;
			}
			OS.gtk_tree_path_free (data);
			list = OS.g_list_next (list);
		}
		OS.g_list_free (originalList);
		TableItem [] result = new TableItem [length];
		for (int i=0; i<result.length; i++) result [i] = _getItem (treeSelection [i]);
		return result;
	}
	return new TableItem [0];
}

/**
 * Returns the number of selected items contained in the receiver.
 *
 * @return the number of selected items
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getSelectionCount () {
	checkWidget();
	long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
	return OS.gtk_tree_selection_count_selected_rows (selection);
}

/**
 * Returns the zero-relative index of the item which is currently
 * selected in the receiver, or -1 if no item is selected.
 *
 * @return the index of the selected item
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getSelectionIndex () {
	checkWidget();
	long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
	long /*int*/ list = OS.gtk_tree_selection_get_selected_rows (selection, null);
	long /*int*/ originalList = list;
	if (list != 0) {
		int [] index = new int [1];
		boolean foundIndex = false;
		while (list != 0) {
			long /*int*/ data = OS.g_list_data (list);
			if (foundIndex == false) {
				long /*int*/ indices = OS.gtk_tree_path_get_indices (data);
				if (indices != 0) {
					OS.memmove (index, indices, 4);
					foundIndex = true;
				}
			}
			list = OS.g_list_next (list);
			OS.gtk_tree_path_free (data);
		}
		OS.g_list_free (originalList);
		return index [0];
	}
	return -1;
}

/**
 * Returns the zero-relative indices of the items which are currently
 * selected in the receiver. The order of the indices is unspecified.
 * The array is empty if no items are selected.
 * <p>
 * Note: This is not the actual structure used by the receiver
 * to maintain its selection, so modifying the array will
 * not affect the receiver.
 * </p>
 * @return the array of indices of the selected items
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int [] getSelectionIndices () {
	checkWidget();
	long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
	long /*int*/ list = OS.gtk_tree_selection_get_selected_rows (selection, null);
	long /*int*/ originalList = list;
	if (list != 0) {
		int count = OS.g_list_length (list);
		int [] treeSelection = new int [count];
		int length = 0;
		for (int i=0; i<count; i++) {
			long /*int*/ data = OS.g_list_data (list);
			long /*int*/ indices = OS.gtk_tree_path_get_indices (data);
			if (indices != 0) {
				int [] index = new int [1];
				OS.memmove (index, indices, 4);
				treeSelection [length] = index [0];
				length++;
			}
			list = OS.g_list_next (list);
			OS.gtk_tree_path_free (data);
		}
		OS.g_list_free (originalList);
		int [] result = new int [length];
		System.arraycopy (treeSelection, 0, result, 0, length);
		return result;
	}
	return new int [0];
}

/**
 * Returns the column which shows the sort indicator for
 * the receiver. The value may be null if no column shows
 * the sort indicator.
 *
 * @return the sort indicator
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #setSortColumn(TableColumn)
 *
 * @since 3.2
 */
public TableColumn getSortColumn () {
	checkWidget ();
	return sortColumn;
}

/**
 * Returns the direction of the sort indicator for the receiver.
 * The value will be one of <code>UP</code>, <code>DOWN</code>
 * or <code>NONE</code>.
 *
 * @return the sort direction
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #setSortDirection(int)
 *
 * @since 3.2
 */
public int getSortDirection () {
	checkWidget ();
	return sortDirection;
}

long /*int*/ getTextRenderer (long /*int*/ column) {
	long /*int*/ list = OS.gtk_cell_layout_get_cells(column);
	if (list == 0) return 0;
	long /*int*/ originalList = list;
	long /*int*/ textRenderer = 0;
	while (list != 0) {
		long /*int*/ renderer = OS.g_list_data (list);
		 if (OS.GTK_IS_CELL_RENDERER_TEXT (renderer)) {
			textRenderer = renderer;
			break;
		}
		list = OS.g_list_next (list);
	}
	OS.g_list_free (originalList);
	return textRenderer;
}

/**
 * Returns the zero-relative index of the item which is currently
 * at the top of the receiver. This index can change when items are
 * scrolled or new items are added or removed.
 *
 * @return the index of the top item
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getTopIndex () {
	checkWidget();
	long /*int*/ [] path = new long /*int*/ [1];
	OS.gtk_widget_realize (handle);
	if (!OS.gtk_tree_view_get_path_at_pos (handle, 1, 1, path, null, null, null)) return 0;
	if (path [0] == 0) return 0;
	long /*int*/ indices = OS.gtk_tree_path_get_indices (path[0]);
	int[] index = new int [1];
	if (indices != 0) OS.memmove (index, indices, 4);
	OS.gtk_tree_path_free (path [0]);
	return index [0];
}

@Override
long /*int*/ gtk_button_press_event (long /*int*/ widget, long /*int*/ event) {
	GdkEventButton gdkEvent = new GdkEventButton ();
	OS.memmove (gdkEvent, event, GdkEventButton.sizeof);
	if (gdkEvent.window != OS.gtk_tree_view_get_bin_window (handle)) return 0;
	long /*int*/ result = super.gtk_button_press_event (widget, event);
	if (result != 0) return result;
	/*
	* Feature in GTK.  In a multi-select table view, when multiple items are already
	* selected, the selection state of the item is toggled and the previous selection
	* is cleared. This is not the desired behaviour when bringing up a popup menu.
	* Also, when an item is reselected with the right button, the tree view issues
	* an unwanted selection event. The workaround is to detect that case and not
	* run the default handler when the item is already part of the current selection.
	*/
	int button = gdkEvent.button;
	if (button == 3 && gdkEvent.type == OS.GDK_BUTTON_PRESS) {
		long /*int*/ [] path = new long /*int*/ [1];
		if (OS.gtk_tree_view_get_path_at_pos (handle, (int)gdkEvent.x, (int)gdkEvent.y, path, null, null, null)) {
			if (path [0] != 0) {
				long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
				if (OS.gtk_tree_selection_path_is_selected (selection, path [0])) result = 1;
				OS.gtk_tree_path_free (path [0]);
			}
		}
	}

	/*
	* Feature in GTK.  When the user clicks in a single selection GtkTreeView
	* and there are no selected items, the first item is selected automatically
	* before the click is processed, causing two selection events.  The is fix
	* is the set the cursor item to be same as the clicked item to stop the
	* widget from automatically selecting the first item.
	*/
	if ((style & SWT.SINGLE) != 0 && getSelectionCount () == 0) {
		long /*int*/ [] path = new long /*int*/ [1];
		if (OS.gtk_tree_view_get_path_at_pos (handle, (int)gdkEvent.x, (int)gdkEvent.y, path, null, null, null)) {
			if (path [0] != 0) {
				long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
				OS.g_signal_handlers_block_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
				OS.gtk_tree_view_set_cursor (handle, path [0], 0, false);
				OS.g_signal_handlers_unblock_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
				OS.gtk_tree_path_free (path [0]);
			}
		}
	}

	//If Mouse double-click pressed, manually send a DefaultSelection.  See Bug 312568.
	if (gdkEvent.type == OS.GDK_2BUTTON_PRESS) {
		sendTreeDefaultSelection ();
	}

	return result;
}

@Override
long /*int*/ gtk_key_press_event (long /*int*/ widget, long /*int*/ event) {
	GdkEventKey keyEvent = new GdkEventKey ();
	OS.memmove (keyEvent, event, GdkEventKey.sizeof);
	int key = keyEvent.keyval;
	keyPressDefaultSelectionHandler (event, key);
	return super.gtk_key_press_event (widget, event);
}

/**
 * Used to emulate DefaultSelection event. See Bug 312568.
 * @param event the gtk key press event that was fired.
 */
void keyPressDefaultSelectionHandler (long /*int*/ event, int key) {

	int keymask = gdk_event_get_state (event);
	switch (key) {
		case OS.GDK_Return:
			//Send Default selection return only when no other modifier is pressed.
			if ((keymask & (OS.GDK_MOD1_MASK | OS.GDK_SHIFT_MASK | OS.GDK_CONTROL_MASK
							| OS.GDK_SUPER_MASK | OS.GDK_META_MASK | OS.GDK_HYPER_MASK)) == 0) {
				sendTreeDefaultSelection ();
			}
			break;
		case OS.GDK_space:
			//Shift + Space is a legal DefaultSelection event. (as per row-activation signal documentation).
			//But do not send if another modifier is pressed.
			if ((keymask & (OS.GDK_MOD1_MASK | OS.GDK_CONTROL_MASK
					| OS.GDK_SUPER_MASK | OS.GDK_META_MASK | OS.GDK_HYPER_MASK)) == 0) {
				sendTreeDefaultSelection ();
			}
			break;
	}
}

//Used to emulate DefaultSelection event. See Bug 312568.
//Feature in GTK. 'row-activation' event comes before DoubleClick event.
//This is causing the editor not to get focus after doubleclick.
//The solution is to manually send the DefaultSelection event after a doubleclick,
//and to emulate it for Space/Return.
void sendTreeDefaultSelection() {

	//Note, similar DefaultSelectionHandling in SWT List/Table/Tree
	TableItem tableItem = getFocusItem ();
	if (tableItem == null)
		return;

	Event event = new Event ();
	event.item = tableItem;

	sendSelectionEvent (SWT.DefaultSelection, event, false);
}


@Override
long /*int*/ gtk_button_release_event (long /*int*/ widget, long /*int*/ event) {
	long /*int*/ window = OS.GDK_EVENT_WINDOW (event);
	if (window != OS.gtk_tree_view_get_bin_window (handle)) return 0;
	return super.gtk_button_release_event (widget, event);
}

@Override
long /*int*/ gtk_changed (long /*int*/ widget) {
	TableItem item = getFocusItem ();
	if (item != null) {
		Event event = new Event ();
		event.item = item;
		sendSelectionEvent (SWT.Selection, event, false);
	}
	return 0;
}

@Override
long /*int*/ gtk_event_after (long /*int*/ widget, long /*int*/ gdkEvent) {
	switch (OS.GDK_EVENT_TYPE (gdkEvent)) {
		case OS.GDK_EXPOSE: {
			/*
			* Bug in GTK. SWT connects the expose-event 'after' the default
			* handler of the signal. If the tree has no children, then GTK
			* sends expose signal only 'before' the default signal handler.
			* The fix is to detect this case in 'event_after' and send the
			* expose event.
			*/
			if (OS.gtk_tree_model_iter_n_children (modelHandle, 0) == 0) {
				gtk_expose_event (widget, gdkEvent);
			}
			break;
		}
	}
	return super.gtk_event_after (widget, gdkEvent);
}

void drawInheritedBackground (long /*int*/ eventPtr, long /*int*/ cairo) {
	if ((state & PARENT_BACKGROUND) != 0 || backgroundImage != null) {
		Control control = findBackgroundControl ();
		if (control != null) {
			long /*int*/ window = OS.gtk_tree_view_get_bin_window (handle);
			long /*int*/ rgn = 0;
			if (eventPtr != 0) {
				GdkEventExpose gdkEvent = new GdkEventExpose ();
				OS.memmove (gdkEvent, eventPtr, GdkEventExpose.sizeof);
				if (window != gdkEvent.window) return;
				rgn = gdkEvent.region;
			}
			int [] width = new int [1], height = new int [1];
			gdk_window_get_size (window, width, height);
			int bottom = 0;
			if (itemCount != 0) {
				long /*int*/ iter = OS.g_malloc (OS.GtkTreeIter_sizeof ());
				OS.gtk_tree_model_iter_nth_child (modelHandle, iter, 0, itemCount - 1);
				long /*int*/ path = OS.gtk_tree_model_get_path (modelHandle, iter);
				GdkRectangle rect = new GdkRectangle ();
				OS.gtk_tree_view_get_cell_area (handle, path, 0, rect);
				bottom = rect.y + rect.height;
				OS.gtk_tree_path_free (path);
				OS.g_free (iter);
			}
			if (height [0] > bottom) {
				drawBackground (control, window, cairo, rgn, 0, bottom, width [0], height [0] - bottom);
			}
		}
	}
}

@Override
long /*int*/ gtk_draw (long /*int*/ widget, long /*int*/ cairo) {
	if ((state & OBSCURED) != 0) return 0;
	drawInheritedBackground (0, cairo);
	return super.gtk_draw (widget, cairo);
}

@Override
long /*int*/ gtk_expose_event (long /*int*/ widget, long /*int*/ eventPtr) {
	if ((state & OBSCURED) != 0) return 0;
	drawInheritedBackground (eventPtr, 0);
	return super.gtk_expose_event (widget, eventPtr);
}

@Override
long /*int*/ gtk_motion_notify_event (long /*int*/ widget, long /*int*/ event) {
	long /*int*/ window = OS.GDK_EVENT_WINDOW (event);
	if (window != OS.gtk_tree_view_get_bin_window (handle)) return 0;
	return super.gtk_motion_notify_event (widget, event);
}

@Override
long /*int*/ gtk_row_deleted (long /*int*/ model, long /*int*/ path) {
	if (ignoreAccessibility) {
		OS.g_signal_stop_emission_by_name (model, OS.row_deleted);
	}
	return 0;
}

@Override
long /*int*/ gtk_row_inserted (long /*int*/ model, long /*int*/ path, long /*int*/ iter) {
	if (ignoreAccessibility) {
		OS.g_signal_stop_emission_by_name (model, OS.row_inserted);
	}
	return 0;
}
@Override
long /*int*/ gtk_start_interactive_search(long /*int*/ widget) {
	if (!searchEnabled()) {
		OS.g_signal_stop_emission_by_name(widget, OS.start_interactive_search);
		return 1;
	}
	return 0;
}
@Override
long /*int*/ gtk_toggled (long /*int*/ renderer, long /*int*/ pathStr) {
	long /*int*/ path = OS.gtk_tree_path_new_from_string (pathStr);
	if (path == 0) return 0;
	long /*int*/ indices = OS.gtk_tree_path_get_indices (path);
	if (indices != 0) {
		int [] index = new int [1];
		OS.memmove (index, indices, 4);
		TableItem item = _getItem (index [0]);
		item.setChecked (!item.getChecked ());
		Event event = new Event ();
		event.detail = SWT.CHECK;
		event.item = item;
		sendSelectionEvent (SWT.Selection, event, false);
	}
	OS.gtk_tree_path_free (path);
	return 0;
}

@Override
void gtk_widget_size_request (long /*int*/ widget, GtkRequisition requisition) {
	/*
	 * Bug in GTK.  For some reason, gtk_widget_size_request() fails
	 * to include the height of the tree view items when there are
	 * no columns visible.  The fix is to temporarily make one column
	 * visible.
	 */
	if (columnCount == 0) {
		super.gtk_widget_size_request (widget, requisition);
		return;
	}
	long /*int*/ columns = OS.gtk_tree_view_get_columns (handle), list = columns;
	boolean fixVisible = columns != 0;
	while (list != 0) {
		long /*int*/ column = OS.g_list_data (list);
		if (OS.gtk_tree_view_column_get_visible (column)) {
			fixVisible = false;
			break;
		}
		list = OS.g_list_next (list);
	}
	long /*int*/ columnHandle = 0;
	if (fixVisible) {
		columnHandle = OS.g_list_data (columns);
		OS.gtk_tree_view_column_set_visible (columnHandle, true);
	}
	super.gtk_widget_size_request (widget, requisition);
	if (fixVisible) {
		OS.gtk_tree_view_column_set_visible (columnHandle, false);
	}
	if (columns != 0) OS.g_list_free (columns);
}

void hideFirstColumn () {
	long /*int*/ firstColumn = OS.gtk_tree_view_get_column (handle, 0);
	OS.gtk_tree_view_column_set_visible (firstColumn, false);
}

@Override
void hookEvents () {
	super.hookEvents ();
	long /*int*/ selection = OS.gtk_tree_view_get_selection(handle);
	OS.g_signal_connect_closure (selection, OS.changed, display.getClosure (CHANGED), false);
	OS.g_signal_connect_closure (handle, OS.row_activated, display.getClosure (ROW_ACTIVATED), false);
	if (checkRenderer != 0) {
		OS.g_signal_connect_closure (checkRenderer, OS.toggled, display.getClosure (TOGGLED), false);
	}
	OS.g_signal_connect_closure (handle, OS.start_interactive_search, display.getClosure (START_INTERACTIVE_SEARCH), false);
	if (fixAccessibility ()) {
		OS.g_signal_connect_closure (modelHandle, OS.row_inserted, display.getClosure (ROW_INSERTED), true);
		OS.g_signal_connect_closure (modelHandle, OS.row_deleted, display.getClosure (ROW_DELETED), true);
	}
}

/**
 * Searches the receiver's list starting at the first column
 * (index 0) until a column is found that is equal to the
 * argument, and returns the index of that column. If no column
 * is found, returns -1.
 *
 * @param column the search column
 * @return the index of the column
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the column is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int indexOf (TableColumn column) {
	checkWidget();
	if (column == null) error (SWT.ERROR_NULL_ARGUMENT);
	for (int i=0; i<columnCount; i++) {
		if (columns [i] == column) return i;
	}
	return -1;
}

/**
 * Searches the receiver's list starting at the first item
 * (index 0) until an item is found that is equal to the
 * argument, and returns the index of that item. If no item
 * is found, returns -1.
 *
 * @param item the search item
 * @return the index of the item
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the item is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int indexOf (TableItem item) {
	checkWidget();
	if (item == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (1 <= lastIndexOf && lastIndexOf < itemCount - 1) {
		if (items [lastIndexOf] == item) return lastIndexOf;
		if (items [lastIndexOf + 1] == item) return ++lastIndexOf;
		if (items [lastIndexOf - 1] == item) return --lastIndexOf;
	}
	if (lastIndexOf < itemCount / 2) {
		for (int i=0; i<itemCount; i++) {
			if (items [i] == item) return lastIndexOf = i;
		}
	} else {
		for (int i=itemCount - 1; i>=0; --i) {
			if (items [i] == item) return lastIndexOf = i;
		}
	}
	return -1;
}

/**
 * Returns <code>true</code> if the item is selected,
 * and <code>false</code> otherwise.  Indices out of
 * range are ignored.
 *
 * @param index the index of the item
 * @return the selection state of the item at the index
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean isSelected (int index) {
	checkWidget();
	long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
	byte [] buffer = Converter.wcsToMbcs (null, Integer.toString (index), true);
	long /*int*/ path = OS.gtk_tree_path_new_from_string (buffer);
	boolean answer = OS.gtk_tree_selection_path_is_selected (selection, path);
	OS.gtk_tree_path_free (path);
	return answer;
}

@Override
boolean mnemonicHit (char key) {
	for (int i=0; i<columnCount; i++) {
		long /*int*/ labelHandle = columns [i].labelHandle;
		if (labelHandle != 0 && mnemonicHit (labelHandle, key)) return true;
	}
	return false;
}

@Override
boolean mnemonicMatch (char key) {
	for (int i=0; i<columnCount; i++) {
		long /*int*/ labelHandle = columns [i].labelHandle;
		if (labelHandle != 0 && mnemonicMatch (labelHandle, key)) return true;
	}
	return false;
}

@Override
long /*int*/ paintWindow () {
	OS.gtk_widget_realize (handle);
	if (fixedHandle != 0 && OS.GTK_VERSION > OS.VERSION(3, 9, 0)) {
		OS.gtk_widget_realize (fixedHandle);
		return OS.gtk_widget_get_window(fixedHandle);
	}
	return OS.gtk_tree_view_get_bin_window (handle);
}

void recreateRenderers () {
	if (checkRenderer != 0) {
		display.removeWidget (checkRenderer);
		OS.g_object_unref (checkRenderer);
		checkRenderer = ownerDraw ? OS.g_object_new (display.gtk_cell_renderer_toggle_get_type(), 0) : OS.gtk_cell_renderer_toggle_new ();
		if (checkRenderer == 0) error (SWT.ERROR_NO_HANDLES);
		OS.g_object_ref (checkRenderer);
		display.addWidget (checkRenderer, this);
		OS.g_signal_connect_closure (checkRenderer, OS.toggled, display.getClosure (TOGGLED), false);
	}
	if (columnCount == 0) {
		createRenderers (OS.gtk_tree_view_get_column (handle, 0), Table.FIRST_COLUMN, true, 0);
	} else {
		for (int i = 0; i < columnCount; i++) {
			TableColumn column = columns [i];
			createRenderers (column.handle, column.modelIndex, i == 0, column.style);
		}
	}
}

@Override
void redrawBackgroundImage () {
	Control control = findBackgroundControl ();
	if (control != null && control.backgroundImage != null) {
		redrawWidget (0, 0, 0, 0, true, false, false);
	}
}

@Override
void register () {
	super.register ();
	display.addWidget (OS.gtk_tree_view_get_selection (handle), this);
	if (checkRenderer != 0) display.addWidget (checkRenderer, this);
	display.addWidget (modelHandle, this);
}

@Override
void releaseChildren (boolean destroy) {
	if (items != null) {
		for (int i=0; i<itemCount; i++) {
			TableItem item = items [i];
			if (item != null && !item.isDisposed ()) {
				item.release (false);
			}
		}
		items = null;
	}
	if (columns != null) {
		for (int i=0; i<columnCount; i++) {
			TableColumn column = columns [i];
			if (column != null && !column.isDisposed ()) {
				column.release (false);
			}
		}
		columns = null;
	}
	super.releaseChildren (destroy);
}

@Override
void releaseWidget () {
	super.releaseWidget ();
	if (modelHandle != 0) OS.g_object_unref (modelHandle);
	modelHandle = 0;
	if (checkRenderer != 0) OS.g_object_unref (checkRenderer);
	checkRenderer = 0;
	if (imageList != null) imageList.dispose ();
	if (headerImageList != null) headerImageList.dispose ();
	imageList = headerImageList = null;
	currentItem = null;
}

/**
 * Removes the item from the receiver at the given
 * zero-relative index.
 *
 * @param index the index for the item
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void remove (int index) {
	checkWidget();
	if (!(0 <= index && index < itemCount)) error (SWT.ERROR_ITEM_NOT_REMOVED);
	long /*int*/ iter = OS.g_malloc (OS.GtkTreeIter_sizeof ());
	TableItem item = items [index];
	boolean disposed = false;
	if (item != null) {
		disposed = item.isDisposed ();
		if (!disposed) {
			OS.memmove (iter, item.handle, OS.GtkTreeIter_sizeof ());
			item.release (false);
		}
	} else {
		OS.gtk_tree_model_iter_nth_child (modelHandle, iter, 0, index);
	}
	if (!disposed) {
		long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
		OS.g_signal_handlers_block_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
		OS.gtk_list_store_remove (modelHandle, iter);
		OS.g_signal_handlers_unblock_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
		System.arraycopy (items, index + 1, items, index, --itemCount - index);
		items [itemCount] = null;
	}
	OS.g_free (iter);
}

/**
 * Removes the items from the receiver which are
 * between the given zero-relative start and end
 * indices (inclusive).
 *
 * @param start the start of the range
 * @param end the end of the range
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_RANGE - if either the start or end are not between 0 and the number of elements in the list minus 1 (inclusive)</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void remove (int start, int end) {
	checkWidget();
	if (start > end) return;
	if (!(0 <= start && start <= end && end < itemCount)) {
		error (SWT.ERROR_INVALID_RANGE);
	}
	long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
	long /*int*/ iter = OS.g_malloc (OS.GtkTreeIter_sizeof ());
	if (iter == 0) error (SWT.ERROR_NO_HANDLES);
	if (fixAccessibility ()) {
		ignoreAccessibility = true;
	}
	int index = end;
	while (index >= start) {
		OS.gtk_tree_model_iter_nth_child (modelHandle, iter, 0, index);
		TableItem item = items [index];
		if (item != null && !item.isDisposed ()) item.release (false);
		OS.g_signal_handlers_block_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
		OS.gtk_list_store_remove (modelHandle, iter);
		OS.g_signal_handlers_unblock_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
		index--;
	}
	if (fixAccessibility ()) {
		ignoreAccessibility = false;
		OS.g_object_notify (handle, OS.model);
	}
	OS.g_free (iter);
	index = end + 1;
	System.arraycopy (items, index, items, start, itemCount - index);
	for (int i=itemCount-(index-start); i<itemCount; i++) items [i] = null;
	itemCount = itemCount - (index - start);
}

/**
 * Removes the items from the receiver's list at the given
 * zero-relative indices.
 *
 * @param indices the array of indices of the items
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
 *    <li>ERROR_NULL_ARGUMENT - if the indices array is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void remove (int [] indices) {
	checkWidget();
	if (indices == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (indices.length == 0) return;
	int [] newIndices = new int [indices.length];
	System.arraycopy (indices, 0, newIndices, 0, indices.length);
	sort (newIndices);
	int start = newIndices [newIndices.length - 1], end = newIndices [0];
	if (!(0 <= start && start <= end && end < itemCount)) {
		error (SWT.ERROR_INVALID_RANGE);
	}
	long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
	int last = -1;
	long /*int*/ iter = OS.g_malloc (OS.GtkTreeIter_sizeof ());
	if (iter == 0) error (SWT.ERROR_NO_HANDLES);
	if (fixAccessibility ()) {
		ignoreAccessibility = true;
	}
	for (int i=0; i<newIndices.length; i++) {
		int index = newIndices [i];
		if (index != last) {
			TableItem item = items [index];
			boolean disposed = false;
			if (item != null) {
				disposed = item.isDisposed ();
				if (!disposed) {
					OS.memmove (iter, item.handle, OS.GtkTreeIter_sizeof ());
					item.release (false);
				}
			} else {
				OS.gtk_tree_model_iter_nth_child (modelHandle, iter, 0, index);
			}
			if (!disposed) {
				OS.g_signal_handlers_block_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
				OS.gtk_list_store_remove (modelHandle, iter);
				OS.g_signal_handlers_unblock_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
				System.arraycopy (items, index + 1, items, index, --itemCount - index);
				items [itemCount] = null;
			}
			last = index;
		}
	}
	if (fixAccessibility ()) {
		ignoreAccessibility = false;
		OS.g_object_notify (handle, OS.model);
	}
	OS.g_free (iter);
}

/**
 * Removes all of the items from the receiver.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void removeAll () {
	checkWidget();
	int index = itemCount - 1;
	while (index >= 0) {
		TableItem item = items [index];
		if (item != null && !item.isDisposed ()) item.release (false);
		--index;
	}
	items = new TableItem [4];
	itemCount = 0;
	long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
	OS.g_signal_handlers_block_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
	if (fixAccessibility ()) {
		ignoreAccessibility = true;
	}
	OS.gtk_list_store_clear (modelHandle);
	if (fixAccessibility ()) {
		ignoreAccessibility = false;
		OS.g_object_notify (handle, OS.model);
	}
	OS.g_signal_handlers_unblock_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);

	resetCustomDraw ();
	if (!searchEnabled ()) {
		OS.gtk_tree_view_set_search_column (handle, -1);
	} else {
		/* Set the search column whenever the model changes */
		int firstColumn = columnCount == 0 ? FIRST_COLUMN : columns [0].modelIndex;
		OS.gtk_tree_view_set_search_column (handle, firstColumn + CELL_TEXT);
	}
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when the user changes the receiver's selection.
 *
 * @param listener the listener which should no longer be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see SelectionListener
 * @see #addSelectionListener(SelectionListener)
 */
public void removeSelectionListener(SelectionListener listener) {
	checkWidget();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.Selection, listener);
	eventTable.unhook (SWT.DefaultSelection,listener);
}

void sendMeasureEvent (long /*int*/ cell, long /*int*/ width, long /*int*/ height) {
	if (!ignoreSize && OS.GTK_IS_CELL_RENDERER_TEXT (cell) && hooks (SWT.MeasureItem)) {
		long /*int*/ iter = OS.g_object_get_qdata (cell, Display.SWT_OBJECT_INDEX2);
		TableItem item = null;
		boolean isSelected = false;
		if (iter != 0) {
			long /*int*/ path = OS.gtk_tree_model_get_path (modelHandle, iter);
			int [] buffer = new int [1];
			OS.memmove (buffer, OS.gtk_tree_path_get_indices (path), 4);
			int index = buffer [0];
			item = _getItem (index);
			long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
			isSelected = OS.gtk_tree_selection_path_is_selected (selection, path);
			OS.gtk_tree_path_free (path);
		}
		if (item != null) {
			int columnIndex = 0;
			if (columnCount > 0) {
				long /*int*/ columnHandle = OS.g_object_get_qdata (cell, Display.SWT_OBJECT_INDEX1);
				for (int i = 0; i < columnCount; i++) {
					if (columns [i].handle == columnHandle) {
						columnIndex = i;
						break;
					}
				}
			}
			int [] contentWidth = new int [1], contentHeight = new int  [1];
			if (width != 0) OS.memmove (contentWidth, width, 4);
			if (height != 0) OS.memmove (contentHeight, height, 4);
			if (OS.GTK3) {
				OS.gtk_cell_renderer_get_preferred_height_for_width (cell, handle, contentWidth[0], contentHeight, null);
			}
			Image image = item.getImage (columnIndex);
			int imageWidth = 0;
			if (image != null) {
				Rectangle bounds = image.getBoundsInPixels ();
				imageWidth = bounds.width;
			}
			contentWidth [0] += imageWidth;
			GC gc = new GC (this);
			gc.setFont (item.getFont (columnIndex));
			Event event = new Event ();
			event.item = item;
			event.index = columnIndex;
			event.gc = gc;
			Rectangle eventRect = new Rectangle (0, 0, contentWidth [0], contentHeight [0]);
			event.setBounds (DPIUtil.autoScaleDown (eventRect));
			if (isSelected) event.detail = SWT.SELECTED;
			sendEvent (SWT.MeasureItem, event);
			gc.dispose ();
			Rectangle rect = DPIUtil.autoScaleUp (event.getBounds ());
			contentWidth [0] = rect.width - imageWidth;
			if (contentHeight [0] < rect.height) contentHeight [0] = rect.height;
			if (width != 0) OS.memmove (width, contentWidth, 4);
			if (height != 0) OS.memmove (height, contentHeight, 4);
			if (OS.GTK3) {
				OS.gtk_cell_renderer_set_fixed_size (cell, contentWidth [0], contentHeight [0]);
			}
		}
	}
}

@Override
long /*int*/ rendererGetPreferredWidthProc (long /*int*/ cell, long /*int*/ handle, long /*int*/ minimun_size, long /*int*/ natural_size) {
	long /*int*/ g_class = OS.g_type_class_peek_parent (OS.G_OBJECT_GET_CLASS (cell));
	GtkCellRendererClass klass = new GtkCellRendererClass ();
	OS.memmove (klass, g_class);
	OS.call (klass.get_preferred_width, cell, handle, minimun_size, natural_size);
	sendMeasureEvent (cell, minimun_size, 0);
	return 0;
}

@Override
long /*int*/ rendererGetSizeProc (long /*int*/ cell, long /*int*/ widget, long /*int*/ cell_area, long /*int*/ x_offset, long /*int*/ y_offset, long /*int*/ width, long /*int*/ height) {
	long /*int*/ g_class = OS.g_type_class_peek_parent (OS.G_OBJECT_GET_CLASS (cell));
	GtkCellRendererClass klass = new GtkCellRendererClass ();
	OS.memmove (klass, g_class);
	OS.call_get_size (klass.get_size, cell, handle, cell_area, x_offset, y_offset, width, height);
	sendMeasureEvent (cell, width, height);
	return 0;
}


@Override
long /*int*/ rendererRenderProc (long /*int*/ cell, long /*int*/ cr, long /*int*/ widget, long /*int*/ background_area, long /*int*/ cell_area, long /*int*/ flags) {
	rendererRender (cell, cr, 0, widget, background_area, cell_area, 0, flags);
	return 0;
}

@Override
long /*int*/ rendererRenderProc (long /*int*/ cell, long /*int*/ window, long /*int*/ widget, long /*int*/ background_area, long /*int*/ cell_area, long /*int*/ expose_area, long /*int*/ flags) {
	rendererRender (cell, 0, window, widget, background_area, cell_area, expose_area, flags);
	return 0;
}

void rendererRender (long /*int*/ cell, long /*int*/ cr, long /*int*/ window, long /*int*/ widget, long /*int*/ background_area, long /*int*/ cell_area, long /*int*/ expose_area, long /*int*/ flags) {
	TableItem item = null;
	long /*int*/ iter = OS.g_object_get_qdata (cell, Display.SWT_OBJECT_INDEX2);
	if (iter != 0) {
		long /*int*/ path = OS.gtk_tree_model_get_path (modelHandle, iter);
		int [] buffer = new int [1];
		OS.memmove (buffer, OS.gtk_tree_path_get_indices (path), 4);
		int index = buffer [0];
		item = _getItem (index);
		OS.gtk_tree_path_free (path);
	}
	long /*int*/ columnHandle = OS.g_object_get_qdata (cell, Display.SWT_OBJECT_INDEX1);
	int columnIndex = 0;
	if (columnCount > 0) {
		for (int i = 0; i < columnCount; i++) {
			if (columns [i].handle == columnHandle) {
				columnIndex = i;
				break;
			}
		}
	}
	if (item != null) {
		if (OS.GTK_IS_CELL_RENDERER_TOGGLE (cell) ||
				( (OS.GTK_IS_CELL_RENDERER_PIXBUF (cell) || OS.GTK_VERSION > OS.VERSION(3, 13, 0)) && (columnIndex != 0 || (style & SWT.CHECK) == 0))) {
			drawFlags = (int)/*64*/flags;
			drawState = SWT.FOREGROUND;
			long /*int*/ [] ptr = new long /*int*/ [1];
			OS.gtk_tree_model_get (modelHandle, item.handle, Table.BACKGROUND_COLUMN, ptr, -1);
			if (ptr [0] == 0) {
				int modelIndex = columnCount == 0 ? Table.FIRST_COLUMN : columns [columnIndex].modelIndex;
				OS.gtk_tree_model_get (modelHandle, item.handle, modelIndex + Table.CELL_BACKGROUND, ptr, -1);
			}
			if (ptr [0] != 0) {
				drawState |= SWT.BACKGROUND;
				OS.gdk_color_free (ptr [0]);
			}
			if ((flags & OS.GTK_CELL_RENDERER_SELECTED) != 0) drawState |= SWT.SELECTED;
			if (!OS.GTK3 || (flags & OS.GTK_CELL_RENDERER_SELECTED) == 0) {
				if ((flags & OS.GTK_CELL_RENDERER_FOCUSED) != 0) drawState |= SWT.FOCUSED;
			}

			GdkRectangle rect = new GdkRectangle ();
			long /*int*/ path = OS.gtk_tree_model_get_path (modelHandle, iter);
			OS.gtk_tree_view_get_background_area (handle, path, columnHandle, rect);
			OS.gtk_tree_path_free (path);
			// A workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=459117
			if (cr != 0 && OS.GTK_VERSION > OS.VERSION(3, 9, 0) && OS.GTK_VERSION <= OS.VERSION(3, 14, 8)) {
				GdkRectangle r2 = new GdkRectangle ();
				OS.gdk_cairo_get_clip_rectangle (cr, r2);
				rect.x = r2.x;
				rect.width = r2.width;
			}
			if ((drawState & SWT.SELECTED) == 0) {
				if ((state & PARENT_BACKGROUND) != 0 || backgroundImage != null) {
					Control control = findBackgroundControl ();
					if (control != null) {
						if (cr != 0) {
							Cairo.cairo_save (cr);
							if (!OS.GTK3){
								Cairo.cairo_reset_clip (cr);
							}
						}
						drawBackground (control, window, cr, 0, rect.x, rect.y, rect.width, rect.height);
						if (cr != 0) {
							Cairo.cairo_restore (cr);
						}
					}
				}
			}

			//send out measure before erase
			long /*int*/ textRenderer =  getTextRenderer (columnHandle);
			if (textRenderer != 0) gtk_cell_renderer_get_preferred_size (textRenderer, handle, null, null);


			if (hooks (SWT.EraseItem)) {
				boolean wasSelected = (drawState & SWT.SELECTED) != 0;
				if (wasSelected) {
					Control control = findBackgroundControl ();
					if (control == null) control = this;
					if (!OS.GTK3){
						if (cr != 0) {
							Cairo.cairo_save (cr);
							Cairo.cairo_reset_clip (cr);
						}
						drawBackground (control, window, cr, 0, rect.x, rect.y, rect.width, rect.height);
						if (cr != 0) {
							Cairo.cairo_restore (cr);
						}
					}
				}
				GC gc = getGC(cr);
				if ((drawState & SWT.SELECTED) != 0) {
					gc.setBackground (display.getSystemColor (SWT.COLOR_LIST_SELECTION));
					gc.setForeground (display.getSystemColor (SWT.COLOR_LIST_SELECTION_TEXT));
				} else {
					gc.setBackground (item.getBackground (columnIndex));
					gc.setForeground (item.getForeground (columnIndex));
				}
				gc.setFont (item.getFont (columnIndex));
				if ((style & SWT.MIRRORED) != 0) rect.x = getClientWidth () - rect.width - rect.x;
				if (OS.GTK_VERSION >= OS.VERSION(3, 9, 0) && cr != 0) {
					GdkRectangle r = new GdkRectangle();
					OS.gdk_cairo_get_clip_rectangle(cr, r);
					gc.setClipping(DPIUtil.autoScaleDown(new Rectangle(rect.x, r.y, r.width, r.height)));

					if (OS.GTK_VERSION <= OS.VERSION(3, 14, 8)) {
						rect.width = r.width;
					}
				} else {
					gc.setClipping(DPIUtil.autoScaleDown(new Rectangle(rect.x, rect.y, rect.width, rect.height)));

				}
				Event event = new Event ();
				event.item = item;
				event.index = columnIndex;
				event.gc = gc;
				event.detail = drawState;
				Rectangle eventRect = new Rectangle (rect.x, rect.y, rect.width, rect.height);
				event.setBounds (DPIUtil.autoScaleDown (eventRect));
				sendEvent (SWT.EraseItem, event);
				drawForeground = null;
				drawState = event.doit ? event.detail : 0;
				drawFlags &= ~(OS.GTK_CELL_RENDERER_FOCUSED | OS.GTK_CELL_RENDERER_SELECTED);
				if ((drawState & SWT.SELECTED) != 0) drawFlags |= OS.GTK_CELL_RENDERER_SELECTED;
				if ((drawState & SWT.FOCUSED) != 0) drawFlags |= OS.GTK_CELL_RENDERER_FOCUSED;
				if ((drawState & SWT.SELECTED) != 0) {
					if (OS.GTK3) {
						Cairo.cairo_save (cr);
						Cairo.cairo_reset_clip (cr);
						long /*int*/ context = OS.gtk_widget_get_style_context (widget);
						OS.gtk_style_context_save (context);
						OS.gtk_style_context_add_class (context, OS.GTK_STYLE_CLASS_CELL);
						OS.gtk_style_context_set_state (context, OS.GTK_STATE_FLAG_SELECTED);
						OS.gtk_render_background(context, cr, rect.x, rect.y, rect.width, rect.height);
						OS.gtk_style_context_restore (context);
						Cairo.cairo_restore (cr);
					} else {
						long /*int*/ style = OS.gtk_widget_get_style (widget);
						//TODO - parity and sorted
						byte[] detail = Converter.wcsToMbcs (null, "cell_odd", true);
						OS.gtk_paint_flat_box (style, window, OS.GTK_STATE_SELECTED, OS.GTK_SHADOW_NONE, rect, widget, detail, rect.x, rect.y, rect.width, rect.height);
					}
				} else {
					if (wasSelected) drawForeground = gc.getForeground ().handle;
				}
				gc.dispose();
			}
		}
	}
	if ((drawState & SWT.BACKGROUND) != 0 && (drawState & SWT.SELECTED) == 0) {
		GC gc = getGC(cr);
		gc.setBackground (item.getBackground (columnIndex));
		GdkRectangle rect = new GdkRectangle ();
		OS.memmove (rect, background_area, GdkRectangle.sizeof);
		gc.fillRectangle(DPIUtil.autoScaleDown(new Rectangle(rect.x, rect.y, rect.width, rect.height)));
		gc.dispose ();
	}
	if ((drawState & SWT.FOREGROUND) != 0 || OS.GTK_IS_CELL_RENDERER_TOGGLE (cell)) {
		long /*int*/ g_class = OS.g_type_class_peek_parent (OS.G_OBJECT_GET_CLASS (cell));
		GtkCellRendererClass klass = new GtkCellRendererClass ();
		OS.memmove (klass, g_class);
		if (drawForeground != null && OS.GTK_IS_CELL_RENDERER_TEXT (cell)) {
			OS.g_object_set (cell, OS.foreground_gdk, drawForeground, 0);
		}
		if (OS.GTK3) {
			OS.call (klass.render, cell, cr, widget, background_area, cell_area, drawFlags);
		} else {
			OS.call (klass.render, cell, window, widget, background_area, cell_area, expose_area, drawFlags);
		}
	}
	if (item != null) {
		if (OS.GTK_IS_CELL_RENDERER_TEXT (cell)) {
			if (hooks (SWT.PaintItem)) {
				GdkRectangle rect = new GdkRectangle ();
				long /*int*/ path = OS.gtk_tree_model_get_path (modelHandle, iter);
				OS.gtk_tree_view_get_background_area (handle, path, columnHandle, rect);
				OS.gtk_tree_path_free (path);
				// A workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=459117
				if (cr != 0 && OS.GTK_VERSION > OS.VERSION(3, 9, 0) && OS.GTK_VERSION <= OS.VERSION(3, 14, 8)) {
					GdkRectangle r2 = new GdkRectangle ();
					OS.gdk_cairo_get_clip_rectangle (cr, r2);
					rect.x = r2.x;
					rect.width = r2.width;
				}
				ignoreSize = true;
				int [] contentX = new int [1], contentWidth = new int [1];
				gtk_cell_renderer_get_preferred_size (cell, handle, contentWidth, null);
				OS.gtk_tree_view_column_cell_get_position (columnHandle, cell, contentX, null);
				ignoreSize = false;
				Image image = item.getImage (columnIndex);
				int imageWidth = 0;
				if (image != null) {
					Rectangle bounds = image.getBoundsInPixels ();
					imageWidth = bounds.width;
				}
				// On gtk >3.9 and <3.14.8 the clip rectangle does not have image area into clip rectangle
				// need to adjust clip rectangle with image width
				if (cr != 0 && OS.GTK_VERSION > OS.VERSION(3, 9, 0) && OS.GTK_VERSION <= OS.VERSION(3, 14, 8)) {
					rect.x -= imageWidth;
					rect.width +=imageWidth;
				}
				contentX [0] -= imageWidth;
				contentWidth [0] += imageWidth;
				GC gc = getGC(cr);
				if ((drawState & SWT.SELECTED) != 0) {
					Color background, foreground;
					if (OS.gtk_widget_has_focus (handle) || OS.GTK3) {
						background = display.getSystemColor (SWT.COLOR_LIST_SELECTION);
						foreground = display.getSystemColor (SWT.COLOR_LIST_SELECTION_TEXT);
					} else {
						/*
						 * Feature in GTK. When the widget doesn't have focus, then
						 * gtk_paint_flat_box () changes the background color state_type
						 * to GTK_STATE_ACTIVE. The fix is to use the same values in the GC.
						 */
						background = Color.gtk_new (display, display.COLOR_LIST_SELECTION_INACTIVE);
						foreground = Color.gtk_new (display, display.COLOR_LIST_SELECTION_TEXT_INACTIVE);
					}
					gc.setBackground (background);
					gc.setForeground (foreground);
				} else {
					gc.setBackground (item.getBackground (columnIndex));
					Color foreground = drawForeground != null ? Color.gtk_new (display, drawForeground) : item.getForeground (columnIndex);
					gc.setForeground (foreground);
				}
				gc.setFont (item.getFont (columnIndex));
				if ((style & SWT.MIRRORED) != 0) rect.x = getClientWidth () - rect.width - rect.x;
				gc.setClipping(DPIUtil.autoScaleDown(new Rectangle(rect.x, rect.y, rect.width, rect.height)));
				Event event = new Event ();
				event.item = item;
				event.index = columnIndex;
				event.gc = gc;
				Rectangle eventRect = new Rectangle (rect.x + contentX [0], rect.y, contentWidth [0], rect.height);
				event.setBounds (DPIUtil.autoScaleDown (eventRect));
				event.detail = drawState;
				sendEvent (SWT.PaintItem, event);
				gc.dispose();
			}
		}
	}
}

private GC getGC(long /*int*/ cr) {
	GC gc;
	if (OS.GTK3){
		GCData gcData = new GCData();
		gcData.cairo = cr;
		gc = GC.gtk_new(this, gcData );
	} else {
		gc = new GC (this);
	}
	return gc;
}

void resetCustomDraw () {
	if ((style & SWT.VIRTUAL) != 0 || ownerDraw) return;
	int end = Math.max (1, columnCount);
	for (int i=0; i<end; i++) {
		boolean customDraw = columnCount != 0 ? columns [i].customDraw : firstCustomDraw;
		if (customDraw) {
			long /*int*/ column = OS.gtk_tree_view_get_column (handle, i);
			long /*int*/ textRenderer = getTextRenderer (column);
			OS.gtk_tree_view_column_set_cell_data_func (column, textRenderer, 0, 0, 0);
			if (columnCount != 0) columns [i].customDraw = false;
		}
	}
	firstCustomDraw = false;
}

@Override
void reskinChildren (int flags) {
	if (items != null) {
		for (int i=0; i<itemCount; i++) {
			TableItem item = items [i];
			if (item != null) item.reskin (flags);
		}
	}
	if (columns != null) {
		for (int i=0; i<columnCount; i++) {
			TableColumn column = columns [i];
			if (!column.isDisposed ()) column.reskin (flags);
		}
	}
	super.reskinChildren (flags);
}

boolean searchEnabled () {
	/* Disable searching when using VIRTUAL */
	if ((style & SWT.VIRTUAL) != 0) return false;
	return true;
}

/**
 * Selects the item at the given zero-relative index in the receiver.
 * If the item at the index was already selected, it remains
 * selected. Indices that are out of range are ignored.
 *
 * @param index the index of the item to select
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void select (int index) {
	checkWidget();
	if (!(0 <= index && index < itemCount))  return;
	boolean fixColumn = showFirstColumn ();
	long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
	OS.g_signal_handlers_block_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
	TableItem item = _getItem (index);
	OS.gtk_tree_selection_select_iter (selection, item.handle);
	OS.g_signal_handlers_unblock_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
	if (fixColumn) hideFirstColumn ();
}

/**
 * Selects the items in the range specified by the given zero-relative
 * indices in the receiver. The range of indices is inclusive.
 * The current selection is not cleared before the new items are selected.
 * <p>
 * If an item in the given range is not selected, it is selected.
 * If an item in the given range was already selected, it remains selected.
 * Indices that are out of range are ignored and no items will be selected
 * if start is greater than end.
 * If the receiver is single-select and there is more than one item in the
 * given range, then all indices are ignored.
 * </p>
 *
 * @param start the start of the range
 * @param end the end of the range
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see Table#setSelection(int,int)
 */
public void select (int start, int end) {
	checkWidget ();
	if (end < 0 || start > end || ((style & SWT.SINGLE) != 0 && start != end)) return;
	if (itemCount == 0 || start >= itemCount) return;
	start = Math.max (0, start);
	end = Math.min (end, itemCount - 1);
	boolean fixColumn = showFirstColumn ();
	long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
	OS.g_signal_handlers_block_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
	for (int index=start; index<=end; index++) {
		TableItem item = _getItem (index);
		OS.gtk_tree_selection_select_iter (selection, item.handle);
	}
	OS.g_signal_handlers_unblock_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
	if (fixColumn) hideFirstColumn ();
}

/**
 * Selects the items at the given zero-relative indices in the receiver.
 * The current selection is not cleared before the new items are selected.
 * <p>
 * If the item at a given index is not selected, it is selected.
 * If the item at a given index was already selected, it remains selected.
 * Indices that are out of range and duplicate indices are ignored.
 * If the receiver is single-select and multiple indices are specified,
 * then all indices are ignored.
 * </p>
 *
 * @param indices the array of indices for the items to select
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the array of indices is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see Table#setSelection(int[])
 */
public void select (int [] indices) {
	checkWidget ();
	if (indices == null) error (SWT.ERROR_NULL_ARGUMENT);
	int length = indices.length;
	if (length == 0 || ((style & SWT.SINGLE) != 0 && length > 1)) return;
	boolean fixColumn = showFirstColumn ();
	long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
	OS.g_signal_handlers_block_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
	for (int i=0; i<length; i++) {
		int index = indices [i];
		if (!(0 <= index && index < itemCount)) continue;
		TableItem item = _getItem (index);
		OS.gtk_tree_selection_select_iter (selection, item.handle);
	}
	OS.g_signal_handlers_unblock_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
	if (fixColumn) hideFirstColumn ();
}

/**
 * Selects all of the items in the receiver.
 * <p>
 * If the receiver is single-select, do nothing.
 * </p>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void selectAll () {
	checkWidget();
	if ((style & SWT.SINGLE) != 0) return;
	boolean fixColumn = showFirstColumn ();
	long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
	OS.g_signal_handlers_block_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
	OS.gtk_tree_selection_select_all (selection);
	OS.g_signal_handlers_unblock_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
	if (fixColumn) hideFirstColumn ();
}

void selectFocusIndex (int index) {
	/*
	* Note that this method both selects and sets the focus to the
	* specified index, so any previous selection in the list will be lost.
	* gtk does not provide a way to just set focus to a specified list item.
	*/
	if (!(0 <= index && index < itemCount))  return;
	TableItem item = _getItem (index);
	long /*int*/ path = OS.gtk_tree_model_get_path (modelHandle, item.handle);
	long /*int*/ selection = OS.gtk_tree_view_get_selection (handle);
	OS.g_signal_handlers_block_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
	OS.gtk_tree_view_set_cursor (handle, path, 0, false);
	/*
	* Bug in GTK. For some reason, when an event loop is run from
	* within a key pressed handler and a dialog is displayed that
	* contains a GtkTreeView,  gtk_tree_view_set_cursor() does
	* not set the cursor or select the item.  The fix is to select the
	* item with gtk_tree_selection_select_iter() as well.
	*
	* NOTE: This happens in GTK 2.2.1 and is fixed in GTK 2.2.4.
	*/
	OS.gtk_tree_selection_select_iter (selection, item.handle);
	OS.g_signal_handlers_unblock_matched (selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
	OS.gtk_tree_path_free (path);
}

@Override
void setBackgroundColor (GdkColor color) {
	super.setBackgroundColor (color);
	if (!OS.GTK3) {
		OS.gtk_widget_modify_base (handle, 0, color);
	}
}

@Override
void setBackgroundColor (long /*int*/ context, long /*int*/ handle, GdkRGBA rgba) {
	/* Setting the background color overrides the selected background color.
	 * To prevent this, we need to re-set the default. This can be done with CSS
	 * on GTK3.16+, or by using GtkStateFlags as an argument to
	 * gtk_widget_override_background_color() on versions of GTK3 less than 3.16.
	 */
	if (rgba == null) {
		GdkColor temp = getDisplay().COLOR_LIST_BACKGROUND;
		background = display.toGdkRGBA (temp);
	} else {
		background = rgba;
	}
	GdkColor defaultColor = getDisplay().COLOR_LIST_SELECTION;
	GdkRGBA selectedBackground = display.toGdkRGBA (defaultColor);
	if (OS.GTK_VERSION >= OS.VERSION(3, 16, 0)) {
		String css = "GtkTreeView {background-color: " + gtk_rgba_to_css_string(background) + ";}\n"
				+ "GtkTreeView:selected {background-color: " + gtk_rgba_to_css_string(selectedBackground) + ";}";
		gtk_css_provider_load_from_css(context, css);
	} else {
		super.setBackgroundColor(context, handle, rgba);
		OS.gtk_widget_override_background_color(handle, OS.GTK_STATE_FLAG_SELECTED, selectedBackground);
	}
}

@Override
void setBackgroundPixmap (Image image) {
	ownerDraw = true;
	recreateRenderers ();
}

@Override
int setBounds (int x, int y, int width, int height, boolean move, boolean resize) {
	int result = super.setBounds (x, y, width, height, move, resize);
	/*
	* Bug on GTK.  The tree view sometimes does not get a paint
	* event or resizes to a one pixel square when resized in a new
	* shell that is not visible after any event loop has been run.  The
	* problem is intermittent. It doesn't seem to happen the first time
	* a new shell is created. The fix is to ensure the tree view is realized
	* after it has been resized.
	*/
	OS.gtk_widget_realize (handle);
	return result;
}

/**
 * Sets the order that the items in the receiver should
 * be displayed in to the given argument which is described
 * in terms of the zero-relative ordering of when the items
 * were added.
 *
 * @param order the new order to display the items
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the item order is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the item order is not the same length as the number of items</li>
 * </ul>
 *
 * @see Table#getColumnOrder()
 * @see TableColumn#getMoveable()
 * @see TableColumn#setMoveable(boolean)
 * @see SWT#Move
 *
 * @since 3.1
 */
public void setColumnOrder (int [] order) {
	checkWidget ();
	if (order == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (columnCount == 0) {
		if (order.length > 0) error (SWT.ERROR_INVALID_ARGUMENT);
		return;
	}
	if (order.length != columnCount) error (SWT.ERROR_INVALID_ARGUMENT);
	boolean [] seen = new boolean [columnCount];
	for (int i = 0; i<order.length; i++) {
		int index = order [i];
		if (index < 0 || index >= columnCount) error (SWT.ERROR_INVALID_RANGE);
		if (seen [index]) error (SWT.ERROR_INVALID_ARGUMENT);
		seen [index] = true;
	}
	for (int i=0; i<order.length; i++) {
		long /*int*/ column = columns [order [i]].handle;
		long /*int*/ baseColumn = i == 0 ? 0 : columns [order [i-1]].handle;
		OS.gtk_tree_view_move_column_after (handle, column, baseColumn);
	}
}

@Override
void setFontDescription (long /*int*/ font) {
	super.setFontDescription (font);
	TableColumn[] columns = getColumns ();
	for (int i = 0; i < columns.length; i++) {
		if (columns[i] != null) {
			columns[i].setFontDescription (font);
		}
	}
}

@Override
void setForegroundColor (GdkColor color) {
	setForegroundColor (handle, color, false);
}

/**
 * Marks the receiver's header as visible if the argument is <code>true</code>,
 * and marks it invisible otherwise.
 * <p>
 * If one of the receiver's ancestors is not visible or some
 * other condition makes the receiver not visible, marking
 * it visible may not actually cause it to be displayed.
 * </p>
 *
 * @param show the new visibility state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setHeaderVisible (boolean show) {
	checkWidget ();
	OS.gtk_tree_view_set_headers_visible (handle, show);
}

/**
 * Sets the number of items contained in the receiver.
 *
 * @param count the number of items
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 3.0
 */
public void setItemCount (int count) {
	checkWidget ();
	count = Math.max (0, count);
	if (count == itemCount) return;
	boolean isVirtual = (style & SWT.VIRTUAL) != 0;
	if (!isVirtual) setRedraw (false);
	remove (count, itemCount - 1);
	int length = Math.max (4, (count + 3) / 4 * 4);
	TableItem [] newItems = new TableItem [length];
	System.arraycopy (items, 0, newItems, 0, itemCount);
	items = newItems;
	if (isVirtual) {
		long /*int*/ iter = OS.g_malloc (OS.GtkTreeIter_sizeof ());
		if (iter == 0) error (SWT.ERROR_NO_HANDLES);
		if (fixAccessibility ()) {
			ignoreAccessibility = true;
		}
		for (int i=itemCount; i<count; i++) {
			OS.gtk_list_store_append (modelHandle, iter);
		}
		if (fixAccessibility ()) {
			ignoreAccessibility = false;
			OS.g_object_notify (handle, OS.model);
		}
		OS.g_free (iter);
		itemCount = count;
	} else {
		for (int i=itemCount; i<count; i++) {
			new TableItem (this, SWT.NONE, i, true);
		}
	}
	if (!isVirtual) setRedraw (true);
}

/**
 * Marks the receiver's lines as visible if the argument is <code>true</code>,
 * and marks it invisible otherwise. Note that some platforms draw grid lines
 * while others may draw alternating row colors.
 * <p>
 * If one of the receiver's ancestors is not visible or some
 * other condition makes the receiver not visible, marking
 * it visible may not actually cause it to be displayed.
 * </p>
 *
 * @param show the new visibility state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setLinesVisible (boolean show) {
	checkWidget();
	if (!OS.GTK3) {
		OS.gtk_tree_view_set_rules_hint (handle, show);
	}
	//Note: this is overriden by the active theme in GTK3.
	OS.gtk_tree_view_set_grid_lines (handle, show ? OS.GTK_TREE_VIEW_GRID_LINES_VERTICAL : OS.GTK_TREE_VIEW_GRID_LINES_NONE);
}

void setModel (long /*int*/ newModel) {
	display.removeWidget (modelHandle);
	OS.g_object_unref (modelHandle);
	modelHandle = newModel;
	display.addWidget (modelHandle, this);
	if (fixAccessibility ()) {
		OS.g_signal_connect_closure (modelHandle, OS.row_inserted, display.getClosure (ROW_INSERTED), true);
		OS.g_signal_connect_closure (modelHandle, OS.row_deleted, display.getClosure (ROW_DELETED), true);
	}
}

@Override
void setOrientation (boolean create) {
	super.setOrientation (create);
	for (int i=0; i<itemCount; i++) {
		if (items[i] != null) items[i].setOrientation (create);
	}
	for (int i=0; i<columnCount; i++) {
		if (columns[i] != null) columns[i].setOrientation (create);
	}
}

@Override
void setParentBackground () {
	ownerDraw = true;
	recreateRenderers ();
}

@Override
void setParentWindow (long /*int*/ widget) {
	long /*int*/ window = eventWindow ();
	OS.gtk_widget_set_parent_window (widget, window);
}

@Override
public void setRedraw (boolean redraw) {
	checkWidget();
	super.setRedraw (redraw);
	if (redraw && drawCount == 0) {
		/* Resize the item array to match the item count */
		if (items.length > 4 && items.length - itemCount > 3) {
			int length = Math.max (4, (itemCount + 3) / 4 * 4);
			TableItem [] newItems = new TableItem [length];
			System.arraycopy (items, 0, newItems, 0, itemCount);
			items = newItems;
		}
	}
}

void setScrollWidth (long /*int*/ column, TableItem item) {
	if (columnCount != 0 || currentItem == item) return;
	int width = OS.gtk_tree_view_column_get_fixed_width (column);
	int itemWidth = calculateWidth (column, item.handle);
	if (width < itemWidth) {
		OS.gtk_tree_view_column_set_fixed_width (column, itemWidth);
	}
}

/**
 * Sets the column used by the sort indicator for the receiver. A null
 * value will clear the sort indicator.  The current sort column is cleared
 * before the new column is set.
 *
 * @param column the column used by the sort indicator or <code>null</code>
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the column is disposed</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 3.2
 */
public void setSortColumn (TableColumn column) {
	checkWidget ();
	if (column != null && column.isDisposed ()) error (SWT.ERROR_INVALID_ARGUMENT);
	if (sortColumn != null && !sortColumn.isDisposed()) {
		OS.gtk_tree_view_column_set_sort_indicator (sortColumn.handle, false);
	}
	sortColumn = column;
	if (sortColumn != null && sortDirection != SWT.NONE) {
		OS.gtk_tree_view_column_set_sort_indicator (sortColumn.handle, true);
		OS.gtk_tree_view_column_set_sort_order (sortColumn.handle, sortDirection == SWT.DOWN ? 0 : 1);
	}
}

/**
 * Sets the direction of the sort indicator for the receiver. The value
 * can be one of <code>UP</code>, <code>DOWN</code> or <code>NONE</code>.
 *
 * @param direction the direction of the sort indicator
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 3.2
 */
public void setSortDirection  (int direction) {
	checkWidget ();
	if (direction != SWT.UP && direction != SWT.DOWN && direction != SWT.NONE) return;
	sortDirection = direction;
	if (sortColumn == null || sortColumn.isDisposed ()) return;
	if (sortDirection == SWT.NONE) {
		OS.gtk_tree_view_column_set_sort_indicator (sortColumn.handle, false);
	} else {
		OS.gtk_tree_view_column_set_sort_indicator (sortColumn.handle, true);
		OS.gtk_tree_view_column_set_sort_order (sortColumn.handle, sortDirection == SWT.DOWN ? 0 : 1);
	}
}

/**
 * Selects the item at the given zero-relative index in the receiver.
 * The current selection is first cleared, then the new item is selected,
 * and if necessary the receiver is scrolled to make the new selection visible.
 *
 * @param index the index of the item to select
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see Table#deselectAll()
 * @see Table#select(int)
 */
public void setSelection (int index) {
	checkWidget ();
	boolean fixColumn = showFirstColumn ();
	deselectAll ();
	selectFocusIndex (index);
	showSelection ();
	if (fixColumn) hideFirstColumn ();
}

/**
 * Selects the items in the range specified by the given zero-relative
 * indices in the receiver. The range of indices is inclusive.
 * The current selection is cleared before the new items are selected,
 * and if necessary the receiver is scrolled to make the new selection visible.
 * <p>
 * Indices that are out of range are ignored and no items will be selected
 * if start is greater than end.
 * If the receiver is single-select and there is more than one item in the
 * given range, then all indices are ignored.
 * </p>
 *
 * @param start the start index of the items to select
 * @param end the end index of the items to select
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see Table#deselectAll()
 * @see Table#select(int,int)
 */
public void setSelection (int start, int end) {
	checkWidget ();
	deselectAll();
	if (end < 0 || start > end || ((style & SWT.SINGLE) != 0 && start != end)) return;
	if (itemCount == 0 || start >= itemCount) return;
	boolean fixColumn = showFirstColumn ();
	start = Math.max (0, start);
	end = Math.min (end, itemCount - 1);
	selectFocusIndex (start);
	if ((style & SWT.MULTI) != 0) {
		select (start, end);
	}
	showSelection ();
	if (fixColumn) hideFirstColumn ();
}

/**
 * Selects the items at the given zero-relative indices in the receiver.
 * The current selection is cleared before the new items are selected,
 * and if necessary the receiver is scrolled to make the new selection visible.
 * <p>
 * Indices that are out of range and duplicate indices are ignored.
 * If the receiver is single-select and multiple indices are specified,
 * then all indices are ignored.
 * </p>
 *
 * @param indices the indices of the items to select
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the array of indices is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see Table#deselectAll()
 * @see Table#select(int[])
 */
public void setSelection (int [] indices) {
	checkWidget ();
	if (indices == null) error (SWT.ERROR_NULL_ARGUMENT);
	deselectAll ();
	int length = indices.length;
	if (length == 0 || ((style & SWT.SINGLE) != 0 && length > 1)) return;
	boolean fixColumn = showFirstColumn ();
	selectFocusIndex (indices [0]);
	if ((style & SWT.MULTI) != 0) {
		select (indices);
	}
	showSelection ();
	if (fixColumn) hideFirstColumn ();
}

/**
 * Sets the receiver's selection to the given item.
 * The current selection is cleared before the new item is selected,
 * and if necessary the receiver is scrolled to make the new selection visible.
 * <p>
 * If the item is not in the receiver, then it is ignored.
 * </p>
 *
 * @param item the item to select
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the item is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the item has been disposed</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 3.2
 */
public void setSelection (TableItem item) {
	if (item == null) error (SWT.ERROR_NULL_ARGUMENT);
	setSelection (new TableItem [] {item});
}


/**
 * Sets the receiver's selection to be the given array of items.
 * The current selection is cleared before the new items are selected,
 * and if necessary the receiver is scrolled to make the new selection visible.
 * <p>
 * Items that are not in the receiver are ignored.
 * If the receiver is single-select and multiple items are specified,
 * then all items are ignored.
 * </p>
 *
 * @param items the array of items
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the array of items is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if one of the items has been disposed</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see Table#deselectAll()
 * @see Table#select(int[])
 * @see Table#setSelection(int[])
 */
public void setSelection (TableItem [] items) {
	checkWidget ();
	if (items == null) error (SWT.ERROR_NULL_ARGUMENT);
	boolean fixColumn = showFirstColumn ();
	deselectAll ();
	int length = items.length;
	if (!(length == 0 || ((style & SWT.SINGLE) != 0 && length > 1))) {
		boolean first = true;
		for (int i = 0; i < length; i++) {
			int index = indexOf (items [i]);
			if (index != -1) {
				if (first) {
					first = false;
					selectFocusIndex (index);
				} else {
					select (index);
				}
			}
		}
		showSelection ();
	}
	if (fixColumn) hideFirstColumn ();
}

/**
 * Sets the zero-relative index of the item which is currently
 * at the top of the receiver. This index can change when items
 * are scrolled or new items are added and removed.
 *
 * @param index the index of the top item
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setTopIndex (int index) {
	checkWidget();
	if (!(0 <= index && index < itemCount)) return;
	long /*int*/ path = OS.gtk_tree_model_get_path (modelHandle, _getItem (index).handle);
	OS.gtk_tree_view_scroll_to_cell (handle, path, 0, true, 0f, 0f);
	OS.gtk_tree_path_free (path);
}

/**
 * Shows the column.  If the column is already showing in the receiver,
 * this method simply returns.  Otherwise, the columns are scrolled until
 * the column is visible.
 *
 * @param column the column to be shown
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the column is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the column has been disposed</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @since 3.0
 */
public void showColumn (TableColumn column) {
	checkWidget ();
	if (column == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (column.isDisposed()) error(SWT.ERROR_INVALID_ARGUMENT);
	if (column.parent != this) return;

	OS.gtk_tree_view_scroll_to_cell (handle, 0, column.handle, false, 0, 0);
}

boolean showFirstColumn () {
	/*
	* Bug in GTK.  If no columns are visible, changing the selection
	* will fail.  The fix is to temporarily make a column visible.
	*/
	int columnCount = Math.max (1, this.columnCount);
	for (int i=0; i<columnCount; i++) {
		long /*int*/ column = OS.gtk_tree_view_get_column (handle, i);
		if (OS.gtk_tree_view_column_get_visible (column)) return false;
	}
	long /*int*/ firstColumn = OS.gtk_tree_view_get_column (handle, 0);
	OS.gtk_tree_view_column_set_visible (firstColumn, true);
	return true;
}

/**
 * Shows the item.  If the item is already showing in the receiver,
 * this method simply returns.  Otherwise, the items are scrolled until
 * the item is visible.
 *
 * @param item the item to be shown
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the item is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the item has been disposed</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see Table#showSelection()
 */
public void showItem (TableItem item) {
	checkWidget ();
	if (item == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (item.isDisposed()) error (SWT.ERROR_INVALID_ARGUMENT);
	if (item.parent != this) return;
	showItem (item.handle);
}

void showItem (long /*int*/ iter) {
	long /*int*/ path = OS.gtk_tree_model_get_path (modelHandle, iter);

	OS.gtk_tree_view_scroll_to_cell (handle, path, 0, false, 0, 0);
	OS.gtk_tree_path_free (path);
}

/**
 * Shows the selection.  If the selection is already showing in the receiver,
 * this method simply returns.  Otherwise, the items are scrolled until
 * the selection is visible.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see Table#showItem(TableItem)
 */
public void showSelection () {
	checkWidget();
	TableItem [] selection = getSelection ();
	if (selection.length == 0) return;
	TableItem item = selection [0];
	showItem (item.handle);
}

@Override
void updateScrollBarValue (ScrollBar bar) {
	super.updateScrollBarValue (bar);
	/*
	*  Bug in GTK. Scrolling changes the XWindow position
	* and makes the child widgets appear to scroll even
	* though when queried their position is unchanged.
	* The fix is to queue a resize event for each child to
	* force the position to be corrected.
	*/
	long /*int*/ parentHandle = parentingHandle ();
	long /*int*/ list = OS.gtk_container_get_children (parentHandle);
	if (list == 0) return;
	long /*int*/ temp = list;
	while (temp != 0) {
		long /*int*/ widget = OS.g_list_data (temp);
		if (widget != 0) OS.gtk_widget_queue_resize  (widget);
		temp = OS.g_list_next (temp);
	}
	OS.g_list_free (list);
	if (OS.GTK3) {
		OS.gtk_widget_queue_resize  (handle);
	}
}

@Override
long /*int*/ windowProc (long /*int*/ handle, long /*int*/ arg0, long /*int*/ user_data) {
	switch ((int)/*64*/user_data) {
		case EXPOSE_EVENT_INVERSE: {
			/*
			 * Feature in GTK. When the GtkTreeView has no items it does not propagate
			 * expose events. The fix is to fill the background in the inverse expose
			 * event.
			 */
			if (itemCount == 0 && (state & OBSCURED) == 0) {
				if ((state & PARENT_BACKGROUND) != 0 || backgroundImage != null) {
					Control control = findBackgroundControl ();
					if (control != null) {
						GdkEventExpose gdkEvent = new GdkEventExpose ();
						OS.memmove (gdkEvent, arg0, GdkEventExpose.sizeof);
						long /*int*/ window = OS.gtk_tree_view_get_bin_window (handle);
						if (window == gdkEvent.window) {
							drawBackground (control, window, gdkEvent.region, gdkEvent.area_x, gdkEvent.area_y, gdkEvent.area_width, gdkEvent.area_height);
						}
					}
				}
			}
			break;
		}
	}
	return super.windowProc (handle, arg0, user_data);
}

}
