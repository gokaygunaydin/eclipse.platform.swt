package org.eclipse.swt.widgets;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */

import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.motif.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;

/**
 * Control is the abstract superclass of all windowed user interface classes.
 * <p>
 * <dl>
 * <dt><b>Styles:</b>
 * <dd>BORDER</dd>
 * <dt><b>Events:</b>
 * <dd>FocusIn, FocusOut, Help, KeyDown, KeyUp, MouseDoubleClick, MouseDown, MouseEnter,
 *     MouseExit, MouseHover, MouseUp, MouseMove, Move, Paint, Resize</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation.
 * </p>
 */
public abstract class Control extends Widget implements Drawable {
	Composite parent;
	int fontList;
	Menu menu;
	String toolTipText;
	Object layoutData;
Control () {
	/* Do nothing */
}
/**
 * Constructs a new instance of this class given its parent
 * and a style value describing its behavior and appearance.
 * <p>
 * The style value is either one of the style constants defined in
 * class <code>SWT</code> which is applicable to instances of this
 * class, or must be built by <em>bitwise OR</em>'ing together 
 * (that is, using the <code>int</code> "|" operator) two or more
 * of those <code>SWT</code> style constants. The class description
 * for all SWT widget classes should include a comment which
 * describes the style constants which are applicable to the class.
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
 * @see SWT
 * @see Widget#checkSubclass
 * @see Widget#getStyle
 */
public Control (Composite parent, int style) {
	super (parent, style);
	this.parent = parent;
	createWidget (0);
}
/**
 * Adds the listener to the collection of listeners who will
 * be notified when the control is moved or resized, by sending
 * it one of the messages defined in the <code>ControlListener</code>
 * interface.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see ControlListener
 * @see #removeControlListener
 */
public void addControlListener(ControlListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Resize,typedListener);
	addListener (SWT.Move,typedListener);
}
/**
 * Adds the listener to the collection of listeners who will
 * be notified when the control gains or loses focus, by sending
 * it one of the messages defined in the <code>FocusListener</code>
 * interface.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see FocusListener
 * @see #removeFocusListener
 */
public void addFocusListener(FocusListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener(SWT.FocusIn,typedListener);
	addListener(SWT.FocusOut,typedListener);
}
/**
 * Adds the listener to the collection of listeners who will
 * be notified when the help events are generated for the control, by sending
 * it one of the messages defined in the <code>HelpListener</code>
 * interface.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see HelpListener
 * @see #removeHelpListener
 */
public void addHelpListener (HelpListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Help, typedListener);
}
/**
 * Adds the listener to the collection of listeners who will
 * be notified when keys are pressed and released on the system keyboard, by sending
 * it one of the messages defined in the <code>KeyListener</code>
 * interface.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see KeyListener
 * @see #removeKeyListener
 */
public void addKeyListener(KeyListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener(SWT.KeyUp,typedListener);
	addListener(SWT.KeyDown,typedListener);
}
/**
 * Adds the listener to the collection of listeners who will
 * be notified when mouse buttons are pressed and released, by sending
 * it one of the messages defined in the <code>MouseListener</code>
 * interface.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see MouseListener
 * @see #removeMouseListener
 */
public void addMouseListener(MouseListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener(SWT.MouseDown,typedListener);
	addListener(SWT.MouseUp,typedListener);
	addListener(SWT.MouseDoubleClick,typedListener);
}
/**
 * Adds the listener to the collection of listeners who will
 * be notified when the mouse passes or hovers over controls, by sending
 * it one of the messages defined in the <code>MouseTrackListener</code>
 * interface.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see MouseTrackListener
 * @see #removeMouseTrackListener
 */
public void addMouseTrackListener (MouseTrackListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.MouseEnter,typedListener);
	addListener (SWT.MouseExit,typedListener);
	addListener (SWT.MouseHover,typedListener);
}
/**
 * Adds the listener to the collection of listeners who will
 * be notified when the mouse moves, by sending it one of the
 * messages defined in the <code>MouseMoveListener</code>
 * interface.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see MouseMoveListener
 * @see #removeMouseMoveListener
 */
public void addMouseMoveListener(MouseMoveListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener(SWT.MouseMove,typedListener);
}
/**
 * Adds the listener to the collection of listeners who will
 * be notified when the receiver needs to be painted, by sending it
 * one of the messages defined in the <code>PaintListener</code>
 * interface.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see PaintListener
 * @see #removePaintListener
 */
public void addPaintListener(PaintListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener(SWT.Paint,typedListener);
}
/**
 * Adds the listener to the collection of listeners who will
 * be notified when traversal events occur, by sending it
 * one of the messages defined in the <code>TraverseListener</code>
 * interface.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see TraverseListener
 * @see #removeTraverseListener
 */
public void addTraverseListener (TraverseListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Traverse,typedListener);
}
/**
 * Returns the preferred size of the receiver.
 * <p>
 * The <em>prefered size</em> of a control is the size that it would
 * best be displayed at. The width hint and height hint arguments
 * allow the caller to ask a control questions such as "Given a particular
 * width, how high does the control need to be to show all of the contents?"
 * To indicate that the caller does not wish to constrain a particular 
 * dimension, the constant <code>SWT.DEFAULT</code> is passed for the hint. 
 * </p>
 *
 * @param wHint the width hint (can be <code>SWT.DEFAULT</code>)
 * @param hHint the height hint (can be <code>SWT.DEFAULT</code>)
 * @return the preferred size of the control
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see Layout
 */
public Point computeSize (int wHint, int hHint) {
	return computeSize (wHint, hHint, true);
}
/**
 * Returns the preferred size of the receiver.
 * <p>
 * The <em>prefered size</em> of a control is the size that it would
 * best be displayed at. The width hint and height hint arguments
 * allow the caller to ask a control questions such as "Given a particular
 * width, how high does the control need to be to show all of the contents?"
 * To indicate that the caller does not wish to constrain a particular 
 * dimension, the constant <code>SWT.DEFAULT</code> is passed for the hint. 
 * </p><p>
 * If the changed flag is <code>true</code>, it indicates that the receiver's
 * <em>contents</em> have changed, therefore any caches that a layout manager
 * containing the control may have been keeping need to be flushed. When the
 * control is resized, the changed flag will be <code>false</code>, so layout
 * manager caches can be retained. 
 * </p>
 *
 * @param wHint the width hint (can be <code>SWT.DEFAULT</code>)
 * @param hHint the height hint (can be <code>SWT.DEFAULT</code>)
 * @param changed <code>true</code> if the control's contents have changed, and <code>false</code> otherwise
 * @return the preferred size of the control.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see Layout
 */
public Point computeSize (int wHint, int hHint, boolean changed) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	int width = DEFAULT_WIDTH;
	int height = DEFAULT_HEIGHT;
	if (wHint != SWT.DEFAULT) width = wHint;
	if (hHint != SWT.DEFAULT) height = hHint;
	int border = getBorderWidth ();
	width += border * 2;
	height += border * 2;
	return new Point (width, height);
}

void createWidget (int index) {
	super.createWidget (index);
	/*
	* Feature in MOTIF.  When a widget is created before the
	* parent has been realized, the widget is created behind
	* all siblings in the Z-order.  When a widget is created
	* after the parent parent has been realized, it is created
	* in front of all siblings.  This is not incorrect but is
	* unexpected.  The fix is to force all widgets to always
	* be created behind their siblings.
	*/
	int topHandle = topHandle ();
	if (OS.XtIsRealized (topHandle)) {
		int window = OS.XtWindow (topHandle);
		if (window != 0) {
			int display = OS.XtDisplay (topHandle);
			if (display != 0) OS.XLowerWindow (display, window);
		}
		/*
		* Make that the widget has been properly realized
		* because the widget was created after the parent
		* has been realized.  This is not part of the fix
		* for Z-order in the code above. 
		*/
		realizeChildren ();
	}
	/*
	* Bug in Motif.  When drag and drop is attempted in an
	* Override shell, Motif segment faults.  The fix is to
	* detect this case and disable the drag sequence.
	*/
//	Shell shell = getShell ();
//	int shellHandle = shell.shellHandle;
//	if (OS.XtIsSubclass (shellHandle, OS.OverrideShellWidgetClass ()))  {
//		Display display = getDisplay ();
//		OS.XtOverrideTranslations (handle, display.dragTranslations);
//	}
}
int defaultBackground () {
	return getDisplay ().defaultBackground;
}
int defaultFont () {
	return getDisplay ().defaultFontList;
}
int defaultForeground () {
	return getDisplay ().defaultForeground;
}
void enableHandle (boolean enabled, int widgetHandle) {
	int [] argList = {OS.XmNsensitive, enabled ? 1 : 0};
	OS.XtSetValues (widgetHandle, argList, argList.length / 2);
}
void enableWidget (boolean enabled) {
	enableHandle (enabled, handle);
}
char findMnemonic (String string) {
	int index = 0;
	int length = string.length ();
	do {
		while ((index < length) && (string.charAt (index) != Mnemonic)) index++;
		if (++index >= length) return '\0';
		if (string.charAt (index) != Mnemonic) return string.charAt (index);
		index++;
	} while (index < length);
 	return '\0';
}
int fontHandle () {
	return handle;
}
/**
 * Forces the receiver to have the <em>keyboard focus</em>, causing
 * all keyboard events to be delivered to it.
 *
 * @return <code>true</code> if the control got focus, and <code>false</code> if it was unable to.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #setFocus
 */
public boolean forceFocus () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	Decorations shell = menuShell ();
	shell.setSavedFocus (this);
	shell.bringToTop ();
	return OS.XmProcessTraversal (handle, OS.XmTRAVERSE_CURRENT);
}
/**
 * Returns the receiver's background color.
 *
 * @return the background color
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Color getBackground () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return Color.motif_new (getDisplay (), getXColor (getBackgroundPixel ()));
}
int getBackgroundPixel () {
	int [] argList = {OS.XmNbackground, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	return argList [1];
}
/**
 * Returns the receiver's border width.
 *
 * @return the border width
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getBorderWidth () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	int topHandle = topHandle ();
	int [] argList = {OS.XmNborderWidth, 0};
	OS.XtGetValues (topHandle, argList, argList.length / 2);
	return argList [1];
}
/**
 * Returns a rectangle describing the receiver's size and location
 * relative to its parent (or its display if its parent is null).
 *
 * @return the receiver's bounding rectangle
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Rectangle getBounds () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	int topHandle = topHandle ();
	int [] argList = {OS.XmNx, 0, OS.XmNy, 0, OS.XmNwidth, 0, OS.XmNheight, 0, OS.XmNborderWidth, 0};
	OS.XtGetValues (topHandle, argList, argList.length / 2);
	int borders = argList [9] * 2;
	return new Rectangle ((short) argList [1], (short) argList [3], argList [5] + borders, argList [7] + borders);
}
Point getClientLocation () {
	short [] handle_x = new short [1], handle_y = new short [1];
	OS.XtTranslateCoords (handle, (short) 0, (short) 0, handle_x, handle_y);
	short [] topHandle_x = new short [1], topHandle_y = new short [1];
	OS.XtTranslateCoords (parent.handle, (short) 0, (short) 0, topHandle_x, topHandle_y);
	return new Point (handle_x [0] - topHandle_x [0], handle_y [0] - topHandle_y [0]);
}
/**
 * Returns the display that the receiver was created on.
 *
 * @return the receiver's display
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Display getDisplay () {
	Composite parent = this.parent;
	if (parent == null) error (SWT.ERROR_WIDGET_DISPOSED);
	return parent.getDisplay ();
}
/**
 * Returns <code>true</code> if the receiver is enabled, and
 * <code>false</code> otherwise. A disabled control is typically
 * not selectable from the user interface and draws with an
 * inactive or "grayed" look.
 *
 * @return the receiver's enabled state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean getEnabled () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	int [] argList = {OS.XmNsensitive, 0};
	OS.XtGetValues (topHandle (), argList, argList.length / 2);
	return argList [1] != 0;
}
/**
 * Returns the font that the receiver will use to paint textual information.
 *
 * @return the receiver's font
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Font getFont () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return Font.motif_new (getDisplay (), getFontList ());
}

int getFontAscent () {
	int fontList = getFontList ();
	
	/* Create a font context to iterate over each element in the font list */
	int [] buffer = new int [1];
	if (!OS.XmFontListInitFontContext (buffer, fontList)) {
		error (SWT.ERROR_NO_HANDLES);
	}
	int context = buffer [0];
	
	/* Values discovering during iteration */
	int ascent = 0;
	XFontStruct fontStruct = new XFontStruct ();
	int fontListEntry;
	int [] fontStructPtr = new int [1];
	int [] fontNamePtr = new int [1];
	
	/* Go through each entry in the font list. */
	while ((fontListEntry = OS.XmFontListNextEntry (context)) != 0) {
		int fontPtr = OS.XmFontListEntryGetFont (fontListEntry, buffer);
		if (buffer [0] == 0) { 
			/* FontList contains a single font */
			OS.memmove (fontStruct, fontPtr, XFontStruct.sizeof);
			int fontAscent = Math.max (fontStruct.ascent, fontStruct.max_bounds_ascent);
			if (fontAscent > ascent) ascent = fontAscent;
		} else {
			/* FontList contains a fontSet */
			int nFonts = OS.XFontsOfFontSet (fontPtr, fontStructPtr, fontNamePtr);
			int [] fontStructs = new int [nFonts];
			OS.memmove (fontStructs, fontStructPtr [0], nFonts * 4);
			
			/* Go through each fontStruct in the font set */
			for (int i=0; i<nFonts; i++) { 
				OS.memmove (fontStruct, fontStructs[i], XFontStruct.sizeof);
				int fontAscent = Math.max (fontStruct.ascent, fontStruct.max_bounds_ascent);
				if (fontAscent > ascent) ascent = fontAscent;
			}
		}
	}
	
	OS.XmFontListFreeFontContext (context);
	return ascent;
}

int getFontHeight () {
	int fontList = getFontList ();
	
	/* Create a font context to iterate over each element in the font list */
	int [] buffer = new int [1];
	if (!OS.XmFontListInitFontContext (buffer, fontList)) {
		error (SWT.ERROR_NO_HANDLES);
	}
	int context = buffer [0];
	
	/* Values discovering during iteration */
	int height = 0;
	XFontStruct fontStruct = new XFontStruct ();
	int fontListEntry;
	int [] fontStructPtr = new int [1];
	int [] fontNamePtr = new int [1];
	
	/* Go through each entry in the font list. */
	while ((fontListEntry = OS.XmFontListNextEntry (context)) != 0) {
		int fontPtr = OS.XmFontListEntryGetFont (fontListEntry, buffer);
		if (buffer [0] == 0) { 
			/* FontList contains a single font */
			OS.memmove (fontStruct, fontPtr, XFontStruct.sizeof);
			int fontAscent = Math.max (fontStruct.ascent, fontStruct.max_bounds_ascent);
			int fontDescent = Math.max (fontStruct.descent, fontStruct.max_bounds_descent);
			int fontHeight = fontAscent + fontDescent;
			if (fontHeight > height) height = fontHeight;
		} else {
			/* FontList contains a fontSet */
			int nFonts = OS.XFontsOfFontSet (fontPtr, fontStructPtr, fontNamePtr);
			int [] fontStructs = new int [nFonts];
			OS.memmove (fontStructs, fontStructPtr [0], nFonts * 4);
			
			/* Go through each fontStruct in the font set */
			for (int i=0; i<nFonts; i++) { 
				OS.memmove (fontStruct, fontStructs[i], XFontStruct.sizeof);
				int fontAscent = Math.max (fontStruct.ascent, fontStruct.max_bounds_ascent);
				int fontDescent = Math.max (fontStruct.descent, fontStruct.max_bounds_descent);
				int fontHeight = fontAscent + fontDescent;
				if (fontHeight > height) height = fontHeight;
			}
		}
	}
	
	OS.XmFontListFreeFontContext (context);
	return height;
}
int getFontList () {
	int fontHandle = fontHandle ();
	int [] argList = {OS.XmNfontList, 0};
	OS.XtGetValues (fontHandle, argList, argList.length / 2);
	if (argList [1] != 0) return argList [1];
	if (fontList == 0) fontList = defaultFont ();
	return fontList;
}
/**
 * Returns the foreground color that the receiver will use to draw.
 *
 * @return the receiver's foreground color
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Color getForeground () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return Color.motif_new (getDisplay (), getXColor (getForegroundPixel ()));
}
int getForegroundPixel () {
	int [] argList = {OS.XmNforeground, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	return argList [1];
}
/**
 * Returns layout data which is associated with the receiver.
 *
 * @return the receiver's layout data
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Object getLayoutData () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return layoutData;
}
/**
 * Returns a point describing the receiver's location relative
 * to its parent (or its display if its parent is null).
 *
 * @return the receiver's location
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Point getLocation () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	int topHandle = topHandle ();
	int [] argList = {OS.XmNx, 0, OS.XmNy, 0};
	OS.XtGetValues (topHandle, argList, argList.length / 2);
	return new Point ((short) argList [1], (short) argList [3]);
}
/**
 * Returns the receiver's pop up menu if it has one, or null
 * if it does not. All controls may optionally have a pop up
 * menu that is displayed when the user requests one for
 * the control. The sequence of key strokes, button presses
 * and/or button releases that are used to request a pop up
 * menu is platform specific.
 *
 * @return the receiver's menu
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Menu getMenu () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return menu;
}
/**
 * Returns the receiver's parent, which must be a <code>Composite</code>
 * or null when the receiver is a shell that was created with null or
 * a display for a parent.
 *
 * @return the receiver's parent
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Composite getParent () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return parent;
}
Control [] getPath () {
	int count = 0;
	Shell shell = getShell ();
	Control control = this;
	while (control != shell) {
		count++;
		control = control.parent;
	}
	control = this;
	Control [] result = new Control [count];
	while (control != shell) {
		result [--count] = control;
		control = control.parent;
	}
	return result;
}
/**
 * Returns the receiver's shell. For all controls other than
 * shells, this simply returns the control's nearest ancestor
 * shell. Shells return themselves, even if they are children
 * of other shells.
 *
 * @return the receiver's shell
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #getParent
 */
public Shell getShell () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return parent.getShell ();
}
/**
 * Returns a point describing the receiver's size. The
 * x coordinate of the result is the width of the receiver.
 * The y coordinate of the result is the height of the
 * receiver.
 *
 * @return the receiver's size
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Point getSize () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	int topHandle = topHandle ();
	int [] argList = {OS.XmNwidth, 0, OS.XmNheight, 0, OS.XmNborderWidth, 0};
	OS.XtGetValues (topHandle, argList, argList.length / 2);
	int borders = argList [5] * 2;
	return new Point (argList [1] + borders, argList [3] + borders);
}
/**
 * Returns the receiver's tool tip text, or null if it has
 * not been set.
 *
 * @return the receiver's tool tip text
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public String getToolTipText () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return toolTipText;
}
/**
 * Returns <code>true</code> if the receiver is visible, and
 * <code>false</code> otherwise.
 * <p>
 * If one of the receiver's ancestors is not visible or some
 * other condition makes the receiver not visible, this method
 * may still indicate that it is considered visible even though
 * it may not actually be showing.
 * </p>
 *
 * @return the receiver's visibility state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean getVisible () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	int topHandle = topHandle ();
	int [] argList = {OS.XmNmappedWhenManaged, 0};
	OS.XtGetValues (topHandle, argList, argList.length / 2);
	return argList [1] != 0;
}
XColor getXColor (int pixel) {
	int display = OS.XtDisplay (handle);
	if (display == 0) return null;
	int [] argList = {OS.XmNcolormap, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	int colormap = argList [1];
	if (colormap == 0) return null;
	XColor color = new XColor ();
	color.pixel = pixel;
	OS.XQueryColor (display, colormap, color);
	return color;
}
boolean hasFocus () {
	return this == getDisplay ().getFocusControl ();
}
void hookEvents () {
	int windowProc = getDisplay ().windowProc;
	OS.XtAddEventHandler (handle, OS.KeyPressMask, false, windowProc, SWT.KeyDown);
	OS.XtAddEventHandler (handle, OS.KeyReleaseMask, false, windowProc, SWT.KeyUp);
	OS.XtAddEventHandler (handle, OS.ButtonPressMask, false, windowProc, SWT.MouseDown);
	OS.XtAddEventHandler (handle, OS.ButtonReleaseMask, false, windowProc, SWT.MouseUp);
	OS.XtAddEventHandler (handle, OS.PointerMotionMask, false, windowProc, SWT.MouseMove);
	OS.XtAddEventHandler (handle, OS.EnterWindowMask, false, windowProc, SWT.MouseEnter);
	OS.XtAddEventHandler (handle, OS.LeaveWindowMask, false, windowProc, SWT.MouseExit);
	OS.XtAddEventHandler (handle, OS.ExposureMask, false, windowProc, SWT.Paint);
	OS.XtAddEventHandler (handle, OS.FocusChangeMask, false, windowProc, SWT.FocusIn);
	OS.XtAddCallback (handle, OS.XmNhelpCallback, windowProc, SWT.Help);
}
int inputContext () {
	return getShell ().inputContext ();
}
/**	 
 * Invokes platform specific functionality to allocate a new GC handle.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Control</code>. It is marked public only so that it
 * can be shared within the packages provided by SWT. It is not
 * available on all platforms, and should never be called from
 * application code.
 * </p>
 *
 * @param data the platform specific GC data 
 * @return the platform specific GC handle
 *
 * @private
 */
public int internal_new_GC (GCData data) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!OS.XtIsRealized (handle)) {
		Shell shell = getShell ();
		shell.realizeWidget ();
	}
	int xDisplay = OS.XtDisplay (handle);
	if (xDisplay == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	int xWindow = OS.XtWindow (handle);
	if (xWindow == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	int xGC = OS.XCreateGC (xDisplay, xWindow, 0, null);
	if (xGC == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	OS.XSetGraphicsExposures (xDisplay, xGC, false);
	int [] argList = {OS.XmNforeground, 0, OS.XmNbackground, 0, OS.XmNcolormap, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	if (data != null) {
		data.device = getDisplay ();
		data.display = xDisplay;
		data.drawable = xWindow;
		data.foreground = argList [1];
		data.background = argList [3];
		data.fontList = getFontList ();
		data.colormap = argList [5];
	}
	return xGC;
}
/**	 
 * Invokes platform specific functionality to dispose a GC handle.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Control</code>. It is marked public only so that it
 * can be shared within the packages provided by SWT. It is not
 * available on all platforms, and should never be called from
 * application code.
 * </p>
 *
 * @param handle the platform specific GC handle
 * @param data the platform specific GC data 
 *
 * @private
 */
public void internal_dispose_GC (int xGC, GCData data) {
	int xDisplay = 0;
	if (data != null) xDisplay = data.display;
	if (xDisplay == 0 && handle != 0) xDisplay = OS.XtDisplay (handle);
	if (xDisplay == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	OS.XFreeGC (xDisplay, xGC);
}
/**
 * Returns <code>true</code> if the receiver is enabled, and
 * <code>false</code> otherwise. A disabled control is typically
 * not selectable from the user interface and draws with an
 * inactive or "grayed" look.
 *
 * @return the receiver's enabled state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean isEnabled () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return getEnabled () && parent.isEnabled ();
}
/**
 * Returns <code>true</code> if the receiver has the user-interface
 * focus, and <code>false</code> otherwise.
 *
 * @return the receiver's focus state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean isFocusControl () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return hasFocus ();
}
/**
 * Returns <code>true</code> if the underlying operating
 * system supports this reparenting, otherwise <code>false</code>
 *
 * @return <code>true</code> if the widget can be reparented, otherwise <code>false</code>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean isReparentable () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return false;
}
/**
 * Returns <code>true</code> if the receiver is visible, and
 * <code>false</code> otherwise.
 * <p>
 * If one of the receiver's ancestors is not visible or some
 * other condition makes the receiver not visible, this method
 * may still indicate that it is considered visible even though
 * it may not actually be showing.
 * </p>
 *
 * @return the receiver's visibility state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean isVisible () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return getVisible () && parent.isVisible ();
}
void manageChildren () {
	OS.XtSetMappedWhenManaged (handle, false);
	OS.XtManageChild (handle);
	int [] argList = {OS.XmNborderWidth, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	OS.XtResizeWidget (handle, 1, 1, argList [1]);
	OS.XtSetMappedWhenManaged (handle, true);
}
Decorations menuShell () {
	return parent.menuShell ();
}
boolean mnemonicHit () {
	return false;
}
boolean mnemonicMatch (char key) {
	return false;
}
/**
 * Moves the receiver above the specified control in the
 * drawing order. If the argument is null, then the receiver
 * is moved to the top of the drawing order. The control at
 * the top of the drawing order will not be covered by other
 * controls even if they occupy intersecting areas.
 *
 * @param the sibling control (or null)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void moveAbove (Control control) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	setZOrder (control, true);
}
/**
 * Moves the receiver below the specified control in the
 * drawing order. If the argument is null, then the receiver
 * is moved to the bottom of the drawing order. The control at
 * the bottom of the drawing order will be covered by all other
 * controls which occupy intersecting areas.
 *
 * @param the sibling control (or null)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void moveBelow (Control control) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	setZOrder (control, false);
}
/**
 * Causes the receiver to be resized to its preferred size.
 * For a composite, this involves computing the preferred size
 * from its layout, if there is one.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #computeSize
 */
public void pack () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	pack (true);
}
/**
 * Causes the receiver to be resized to its preferred size.
 * For a composite, this involves computing the preferred size
 * from its layout, if there is one.
 * <p>
 * If the changed flag is <code>true</code>, it indicates that the receiver's
 * <em>contents</em> have changed, therefore any caches that a layout manager
 * containing the control may have been keeping need to be flushed. When the
 * control is resized, the changed flag will be <code>false</code>, so layout
 * manager caches can be retained. 
 * </p>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #computeSize
 */
public void pack (boolean changed) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	setSize (computeSize (SWT.DEFAULT, SWT.DEFAULT, changed));
}
int processDefaultSelection (int callData) {
	postEvent (SWT.DefaultSelection);
	return 0;
}
int processFocusIn () {
	sendEvent (SWT.FocusIn);
//	IsDBLocale ifTrue: [self killImeFocus].
	return 0;
}
int processFocusOut () {
	sendEvent (SWT.FocusOut);
//	IsDBLocale ifTrue: [self killImeFocus].
	return 0;
}
int processHelp (int callData) {
	sendHelpEvent (callData);
	return 0;
}
int processKeyDown (int callData) {
	XKeyEvent xEvent = new XKeyEvent ();
	OS.memmove (xEvent, callData, XKeyEvent.sizeof);
	sendKeyEvent (SWT.KeyDown, xEvent);
	return 0;
}
int processKeyUp (int callData) {
	XKeyEvent xEvent = new XKeyEvent ();
	OS.memmove (xEvent, callData, XKeyEvent.sizeof);
	sendKeyEvent (SWT.KeyUp, xEvent);
	return 0;
}
int processModify (int callData) {
	sendEvent (SWT.Modify);
	return 0;
}
int processMouseDown (int callData) {
	getDisplay ().hideToolTip ();
	XButtonEvent xEvent = new XButtonEvent ();
	OS.memmove (xEvent, callData, XButtonEvent.sizeof);
	sendMouseEvent (SWT.MouseDown, xEvent.button, xEvent.state, xEvent);
	if (xEvent.button == 2 && hooks (SWT.DragDetect)) {
		sendEvent (SWT.DragDetect);
	}
	if (xEvent.button == 3 && menu != null) {
		OS.XmProcessTraversal (handle, OS.XmTRAVERSE_CURRENT);
		menu.setVisible (true);
	}
	int xDisplay = OS.XtDisplay (handle);
	if (xDisplay != 0) {
		Display display = getDisplay ();
		int clickTime = OS.XtGetMultiClickTime (xDisplay);
		int lastTime = display.lastTime, eventTime = xEvent.time;
		int lastButton = display.lastButton, eventButton = xEvent.button;
		if (lastButton == eventButton && lastTime != 0 && Math.abs (lastTime - eventTime) <= clickTime) {
			sendMouseEvent (SWT.MouseDoubleClick, eventButton, xEvent.state, xEvent);
		}
		if (eventTime == 0) eventTime = 1;
		display.lastTime = eventTime;
		display.lastButton = eventButton;
	}
	return 0;
}
int processMouseEnter (int callData) {
	XCrossingEvent xEvent = new XCrossingEvent ();
	OS.memmove (xEvent, callData, XCrossingEvent.sizeof);
	if (xEvent.mode != OS.NotifyNormal) return 0;
	if (xEvent.subwindow != 0) return 0;
	Event event = new Event ();
	event.x = xEvent.x;
	event.y = xEvent.y;
	postEvent (SWT.MouseEnter, event);
	return 0;
}
int processMouseMove (int callData) {
	Display display = getDisplay ();
	display.addMouseHoverTimeOut (handle);
	XMotionEvent xEvent = new XMotionEvent ();
	OS.memmove (xEvent, callData, XMotionEvent.sizeof);
	sendMouseEvent (SWT.MouseMove, 0, xEvent.state, xEvent);
	return 0;
}
int processMouseExit (int callData) {
	Display display = getDisplay ();
	display.removeMouseHoverTimeOut ();
	display.hideToolTip ();
	
	XCrossingEvent xEvent = new XCrossingEvent ();
	OS.memmove (xEvent, callData, XCrossingEvent.sizeof);
	if (xEvent.mode != OS.NotifyNormal) return 0;
	if (xEvent.subwindow != 0) return 0;
	Event event = new Event ();
	event.x = xEvent.x;
	event.y = xEvent.y;
	postEvent (SWT.MouseExit, event);
	return 0;
}
int processMouseUp (int callData) {
	getDisplay ().hideToolTip ();
	XButtonEvent xEvent = new XButtonEvent ();
	OS.memmove (xEvent, callData, XButtonEvent.sizeof);
	sendMouseEvent (SWT.MouseUp, xEvent.button, xEvent.state, xEvent);
	return 0;
}
int processPaint (int callData) {
	if (!hooks (SWT.Paint)) return 0;
	XExposeEvent xEvent = new XExposeEvent ();
	OS.memmove (xEvent, callData, XExposeEvent.sizeof);
	int xDisplay = OS.XtDisplay (handle);
	if (xDisplay == 0) return 0;
	Event event = new Event ();
	event.count = xEvent.count;
	event.time = OS.XtLastTimestampProcessed (xDisplay);
	event.x = xEvent.x;  event.y = xEvent.y;
	event.width = xEvent.width;  event.height = xEvent.height;
	GC gc = event.gc = new GC (this);
	XRectangle rect = new XRectangle ();
	rect.x = (short) xEvent.x;  rect.y = (short) xEvent.y;
	rect.width = (short) xEvent.width;  rect.height = (short) xEvent.height;
	OS.XSetClipRectangles (xDisplay, gc.handle, 0, 0, rect, 1, OS.Unsorted);
	sendEvent (SWT.Paint, event);
	if (!gc.isDisposed ()) gc.dispose ();
	event.gc = null;
	return 0;
}
int processSelection (int callData) {
	postEvent (SWT.Selection);
	return 0;
}
int processSetFocus (int callData) {

	/* Get the focus change event */
	XFocusChangeEvent xEvent = new XFocusChangeEvent ();
	OS.memmove (xEvent, callData, XFocusChangeEvent.sizeof);

	/* Ignore focus changes caused by grabbing and ungrabing. */
	if (xEvent.mode != OS.NotifyNormal) return 0;

	/* Only process focus callbacks between windows. */
	if (xEvent.detail != OS.NotifyAncestor &&
		xEvent.detail != OS.NotifyInferior &&
		xEvent.detail != OS.NotifyNonlinear) return 0;

	/*
	* Ignore focus change events when the window getting or losing
	* focus is a menu.  Because XmGetFocusWidget() does not answer
	* the menu shell (it answers the menu parent), it is necessary
	* to use XGetInputFocus() to get the real X focus window.
	*/
	int xDisplay = xEvent.display;
	if (xDisplay == 0) return 0;
	int [] unused = new int [1], xWindow = new int [1];
	OS.XGetInputFocus (xDisplay, xWindow, unused);
	if (xWindow [0] != 0) {
		int widget = OS.XtWindowToWidget (xDisplay, xWindow [0]);
		if (widget != 0 && OS.XtClass (widget) == OS.XmMenuShellWidgetClass ()) return 0;
	}
	
	/* Process the focus change for the widget. */
	if (xEvent.type == OS.FocusIn) {
		int result = processFocusIn ();
		int index = 0;
		Shell shell = getShell ();
		Control [] focusIn = getPath ();
		Control lastFocus = shell.lastFocus;
		if (lastFocus != null) {
			if (!lastFocus.isDisposed ()) {
				Control [] focusOut = lastFocus.getPath ();
				int length = Math.min (focusIn.length, focusOut.length);
				while (index < length) {
					if (focusIn [index] != focusOut [index]) break;
					index++;
				}
				for (int i=focusOut.length-1; i>=index; --i) {
					focusOut [i].sendEvent (SWT.Deactivate);
				}
			}
			shell.lastFocus = null;
		}
		for (int i=focusIn.length-1; i>=index; --i) {
			focusIn [i].sendEvent (SWT.Activate);
		}
		return result;
	}
	if (xEvent.type == OS.FocusOut) {
		int result = processFocusOut ();
		Shell shell = getShell ();
		shell.lastFocus = this;
		Display display = getDisplay ();
		Control focusControl = display.getFocusControl ();
		if (focusControl == null || shell != focusControl.getShell ()) {
			Control [] focusOut = getPath ();
			for (int i=focusOut.length-1; i>=0; --i) {
				focusOut [i].sendEvent (SWT.Deactivate);
			}
			shell.lastFocus = null;
		}
		return result;
	}
	return 0;
}
void propagateChildren (boolean enabled) {
	propagateWidget (enabled);
}
void propagateHandle (boolean enabled, int widgetHandle) {
	int xDisplay = OS.XtDisplay (widgetHandle);
	if (xDisplay == 0) return;
	int xWindow = OS.XtWindow (widgetHandle);
	if (xWindow == 0) return;
	int event_mask = OS.XtBuildEventMask (widgetHandle);
	int do_not_propagate_mask = OS.KeyPressMask | OS.KeyReleaseMask | OS.ButtonPressMask | OS.ButtonReleaseMask | OS.PointerMotionMask;
	if (!enabled) {
		event_mask &= ~do_not_propagate_mask;
		do_not_propagate_mask = 0;
	}
	XSetWindowAttributes attributes = new XSetWindowAttributes ();
	attributes.event_mask = event_mask;
	attributes.do_not_propagate_mask = do_not_propagate_mask;
	OS.XChangeWindowAttributes (xDisplay, xWindow, OS.CWDontPropagate | OS.CWEventMask, attributes);
	int [] argList = {OS.XmNtraversalOn, enabled ? 1 : 0};
	OS.XtSetValues (widgetHandle, argList, argList.length / 2);
}
void propagateWidget (boolean enabled) {
	propagateHandle (enabled, handle);
}
void realizeChildren () {
	if (!isEnabled ()) propagateWidget (false);
}
/**
 * Causes the entire bounds of the receiver to be marked
 * as needing to be redrawn. The next time a paint request
 * is processed, the control will be completely painted.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #update
 */
public void redraw () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	redrawWidget (0, 0, 0, 0, false);
}
/**
 * Causes the rectangular area of the receiver specified by
 * the arguments to be marked as needing to be redrawn. 
 * The next time a paint request is processed, that area of
 * the receiver will be painted. If the <code>all</code> flag
 * is <code>true</code>, any children of the receiver which
 * intersect with the specified area will also paint their
 * intersecting areas. If the <code>all</code> flag is 
 * <code>false</code>, the children will not be painted.
 *
 * @param x the x coordinate of the area to draw
 * @param y the y coordinate of the area to draw
 * @param width the width of the area to draw
 * @param height the height of the area to draw
 * @param all <code>true</code> if children should redraw, and <code>false</code> otherwise
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #update
 */
public void redraw (int x, int y, int width, int height, boolean all) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (x == 0 && y == 0 && width == 0 && height == 0) return;
	redrawWidget (x, y, width, height, all);
}
void redrawHandle (int x, int y, int width, int height, int widgetHandle) {
	int display = OS.XtDisplay (widgetHandle);
	if (display == 0) return;
	int window = OS.XtWindow (widgetHandle);
	if (window == 0) return;
	int [] argList = {OS.XmNborderWidth, 0, OS.XmNborderColor, 0};
	OS.XtGetValues (widgetHandle, argList, argList.length / 2);
	if (argList [1] != 0) {
		/* Force the border to repaint by setting the color */
		OS.XtSetValues (widgetHandle, argList, argList.length / 2);
	}
	OS.XClearArea (display, window, x, y, width, height, true);
}
void redrawWidget (int x, int y, int width, int height, boolean all) {
	redrawHandle (x, y, width, height, handle);
}
void releaseWidget () {
	super.releaseWidget ();
	Display display = getDisplay ();
	display.releaseToolTipHandle (handle);
	toolTipText = null;
	if (menu != null && !menu.isDisposed ()) {
		menu.dispose ();
	}
	menu = null;
/*
	"Release the IME."
	self isMenu ifFalse: [
		handle xtIsWidget ifTrue: [
			handle xmImUnregister.

			"Bug in X.  On Solaris only, destroying the window that has IME focus causes
			a segment fault.  The fix is to set focus to the IME client window (typically
			the shell) when the receiver is being destroyed.  Destroying the shell window
			does not have the problem.  Note that this fix is not necessary on AIX."
			(xIC := self inputContext) == nil ifFalse: [
				(window := handle xtWindow) isNull ifFalse: [
					xIC xGetICValues: XNFocusWindow with: (buffer := ByteArray new: 4) with: 0.
					(buffer uint32At: 0) = window asInteger ifTrue: [
						xIC xGetICValues: XNClientWindow with: (buffer := ByteArray new: 4) with: 0.
						xIC
							xUnsetICFocus;
							xSetICValues: XNFocusWindow with: (buffer uint32At: 0) with: 0;
							xSetICFocus]]]]].
*/
	parent = null;
	layoutData = null;
}
/**
 * Removes the listener from the collection of listeners who will
 * be notified when the control is moved or resized.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see ControlListener
 * @see #addControlListener
 */
public void removeControlListener (ControlListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.Move, listener);
	eventTable.unhook (SWT.Resize, listener);
}
/**
 * Removes the listener from the collection of listeners who will
 * be notified when the control gains or loses focus.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see FocusListener
 * @see #addFocusListener
 */
public void removeFocusListener(FocusListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook(SWT.FocusIn, listener);
	eventTable.unhook(SWT.FocusOut, listener);
}
/**
 * Removes the listener from the collection of listeners who will
 * be notified when the help events are generated for the control.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see HelpListener
 * @see #addHelpListener
 */
public void removeHelpListener (HelpListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.Help, listener);
}
/**
 * Removes the listener from the collection of listeners who will
 * be notified when keys are pressed and released on the system keyboard.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see KeyListener
 * @see #addKeyListener
 */
public void removeKeyListener(KeyListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook(SWT.KeyUp, listener);
	eventTable.unhook(SWT.KeyDown, listener);
}
/**
 * Removes the listener from the collection of listeners who will
 * be notified when mouse buttons are pressed and released.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see MouseListener
 * @see #addMouseListener
 */
public void removeMouseListener(MouseListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook(SWT.MouseDown, listener);
	eventTable.unhook(SWT.MouseUp, listener);
	eventTable.unhook(SWT.MouseDoubleClick, listener);
}
/**
 * Removes the listener from the collection of listeners who will
 * be notified when the mouse moves.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see MouseMoveListener
 * @see #addMouseMoveListener
 */
public void removeMouseMoveListener(MouseMoveListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook(SWT.MouseMove, listener);
}
/**
 * Removes the listener from the collection of listeners who will
 * be notified when the mouse passes or hovers over controls.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see MouseTrackListener
 * @see #addMouseTrackListener
 */
public void removeMouseTrackListener(MouseTrackListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.MouseEnter, listener);
	eventTable.unhook (SWT.MouseExit, listener);
	eventTable.unhook (SWT.MouseHover, listener);
}
/**
 * Removes the listener from the collection of listeners who will
 * be notified when the receiver needs to be painted.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see PaintListener
 * @see #addPaintListener
 */
public void removePaintListener(PaintListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook(SWT.Paint, listener);
}/**
 * Removes the listener from the collection of listeners who will
 * be notified when traversal events occur.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see TraverseListener
 * @see #addTraverseListener
 */
public void removeTraverseListener(TraverseListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.Traverse, listener);
}
void sendHelpEvent (int callData) {
	Control control = this;
	while (control != null) {
		if (control.hooks (SWT.Help)) {
			control.postEvent (SWT.Help);
			return;
		}
		control = control.parent;
	}
}
void sendKeyEvent (int type, XKeyEvent xEvent) {
	
	/* Look up the keysym and character(s) */
	byte [] buffer;
	boolean isVirtual = false;
	int [] keysym = new int [1], status = new int [1];
	if (xEvent.keycode != 0) {
		buffer = new byte [1];
		isVirtual = OS.XLookupString (xEvent, buffer, buffer.length, keysym, status) == 0;
	} else {
		int size = 0;
		buffer = new byte [2];
		int xIC = inputContext ();
		if (xIC == 0) {
			size = OS.XmImMbLookupString (handle, xEvent, buffer, buffer.length, keysym, status);
			if (status [0] == OS.XBufferOverflow) {
				buffer = new byte [size];
				size = OS.XmImMbLookupString (handle, xEvent, buffer, size, keysym, status);
			}
		} else {
			size = OS.XmbLookupString (xIC, xEvent, buffer, buffer.length, keysym, status);
			if (status [0] == OS.XBufferOverflow) {
				buffer = new byte [size];
				size = OS.XmbLookupString (xIC, xEvent, buffer, size, keysym, status);
			}
		}
		if (size == 0) return;
	}

	/*
	* Bug in MOTIF.  On Solaris only, XK_F11 and XK_F12 are not
	* translated correctly by XLookupString().  They are mapped
	* to 0x1005FF10 and 0x1005FF11 respectively.  The fix is to
	* look for these values explicitly and correct them.
	*/
	if (IsSunOS) {
		if ((keysym [0] == 0x1005FF10) || (keysym [0] == 0x1005FF11)) {
			if (keysym [0] == 0x1005FF10) keysym [0] = OS.XK_F11;
			if (keysym [0] == 0x1005FF11) keysym [0] = OS.XK_F12;
		}
	}
	
	/*
	* Bug in MOTIF.  On Solaris only, their is garbage in the
	* high 16-bits for Keysyms such as XK_Down.  Since Keysyms
	* must be 16-bits to fit into a Character, mask away the
	* high 16-bits on all platforms.
	*/
	keysym [0] &= 0xFFFF;

	/* Convert from MBCS to UNICODE and send the event */
	char [] result = Converter.mbcsToWcs (null, buffer);
	for (int i=0; i<result.length; i++) {
		Event event = new Event ();
		event.time = xEvent.time;
		event.character = result [i];
		if (isVirtual) event.keyCode = Display.translateKey (keysym [0]);
		if ((xEvent.state & OS.Mod1Mask) != 0) event.stateMask |= SWT.ALT;
		if ((xEvent.state & OS.ShiftMask) != 0) event.stateMask |= SWT.SHIFT;
		if ((xEvent.state & OS.ControlMask) != 0) event.stateMask |= SWT.CONTROL;
		if ((xEvent.state & OS.Button1Mask) != 0) event.stateMask |= SWT.BUTTON1;
		if ((xEvent.state & OS.Button2Mask) != 0) event.stateMask |= SWT.BUTTON2;
		if ((xEvent.state & OS.Button3Mask) != 0) event.stateMask |= SWT.BUTTON3;
		postEvent (type, event);
	}
}
void sendMouseEvent (int type, int button, int mask, XWindowEvent xEvent) {
	Event event = new Event ();
	event.time = xEvent.time;
	event.button = button;
	event.x = xEvent.x;  event.y = xEvent.y;
	if ((mask & OS.Mod1Mask) != 0) event.stateMask |= SWT.ALT;
	if ((mask & OS.ShiftMask) != 0) event.stateMask |= SWT.SHIFT;
	if ((mask & OS.ControlMask) != 0) event.stateMask |= SWT.CONTROL;
	if ((mask & OS.Button1Mask) != 0) event.stateMask |= SWT.BUTTON1;
	if ((mask & OS.Button2Mask) != 0) event.stateMask |= SWT.BUTTON2;
	if ((mask & OS.Button3Mask) != 0) event.stateMask |= SWT.BUTTON3;
	postEvent (type, event);
}
/**
 * Sets the receiver's background color to the color specified
 * by the argument, or to the default system color for the control
 * if the argument is null.
 *
 * @param color the new color (or null)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setBackground (Color color) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (color == null) {
		setBackgroundPixel (defaultBackground ());
	} else {
		if (color.isDisposed ()) return;
		setBackgroundPixel (color.handle.pixel);
	}
}
void setBackgroundPixel (int pixel) {
	int [] argList = {OS.XmNforeground, 0, OS.XmNhighlightColor, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	OS.XmChangeColor (handle, pixel);
	OS.XtSetValues (handle, argList, argList.length / 2);
}
/**
 * Sets the receiver's size and location to the rectangular
 * area specified by the arguments. The <code>x</code> and 
 * <code>y</code> arguments are relative to the receiver's
 * parent (or its display if its parent is null).
 * <p>
 * Note: Attempting to set the width or height of the
 * receiver to a negative number will cause that
 * value to be set to zero instead.
 * </p>
 *
 * @param x the new x coordinate for the receiver
 * @param y the new y coordinate for the receiver
 * @param width the new width for the receiver
 * @param height the new height for the receiver
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setBounds (int x, int y, int width, int height) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	/*
	* Feature in Motif.  Motif will not allow a window
	* to have a zero width or zero height.  The fix is
	* to ensure these values are never zero.
	*/
	int topHandle = topHandle ();
	int [] argList = {
		OS.XmNx, 0, 			/* 1 */
		OS.XmNy, 0, 			/* 3 */
		OS.XmNwidth, 0, 		/* 5 */
		OS.XmNheight, 0, 		/* 7 */
		OS.XmNborderWidth, 0, 	/* 9 */
	};
	OS.XtGetValues (topHandle, argList, argList.length / 2);
	int newWidth = Math.max (width - (argList [9] * 2), 1);
	int newHeight = Math.max (height - (argList [9] * 2), 1);
	boolean sameOrigin = (x == (short) argList [1]) && (y == (short) argList [3]);
	boolean sameExtent = (newWidth == argList [5]) && (newHeight == argList [7]);
	if (sameOrigin && sameExtent) return;
	OS.XtConfigureWidget (topHandle, x, y, newWidth, newHeight, argList [9]);
	if (!sameOrigin) sendEvent (SWT.Move);
	if (!sameExtent) sendEvent (SWT.Resize);
}
/**
 * Sets the receiver's size and location to the rectangular
 * area specified by the argument. The <code>x</code> and 
 * <code>y</code> fields of the rectangle are relative to
 * the receiver's parent (or its display if its parent is null).
 * <p>
 * Note: Attempting to set the width or height of the
 * receiver to a negative number will cause that
 * value to be set to zero instead.
 * </p>
 *
 * @param rect the new bounds for the receiver
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setBounds (Rectangle rect) {
	if (rect == null) error (SWT.ERROR_NULL_ARGUMENT);
	setBounds (rect.x, rect.y, rect.width, rect.height);
}
/**
 * If the argument is <code>true</code>, causes the receiver to have
 * all mouse events delivered to it until the method is called with
 * <code>false</code> as the argument.
 *
 * @param capture <code>true</code> to capture the mouse, and <code>false</code> to release it
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setCapture (boolean capture) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	int display = OS.XtDisplay (handle);
	if (display == 0) return;
	if (capture) {
		int window = OS.XtWindow (handle);
		if (window == 0) return;
		OS.XGrabPointer (
			display,
			window,
			0,
			OS.ButtonPressMask | OS.ButtonReleaseMask | OS.PointerMotionMask,
			OS.GrabModeAsync,
			OS.GrabModeAsync,
			OS.None,
			OS.None,
			OS.CurrentTime);
	} else {
		OS.XUngrabPointer (display, OS.CurrentTime);
	}
}
/**
 * Sets the receiver's cursor to the cursor specified by the
 * argument, or to the default cursor for that kind of control
 * if the argument is null.
 * <p>
 * When the mouse pointer passes over a control its appearance
 * is changed to match the control's cursor.
 * </p>
 *
 * @param cursor the new cursor (or null)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setCursor (Cursor cursor) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	int display = OS.XtDisplay (handle);
	if (display == 0) return;
	int window = OS.XtWindow (handle);
	if (window == 0) {
		if (!OS.XtIsRealized (handle)) getShell ().realizeWidget ();
		window = OS.XtWindow (handle);
		if (window == 0) return;
	}
	if (cursor == null) {
		OS.XUndefineCursor (display, window);
	} else {
		int xCursor = cursor.handle;
		OS.XDefineCursor (display, window, xCursor);
		OS.XFlush (display);
	}
}
/**
 * Enables the receiver if the argument is <code>true</code>,
 * and disables it otherwise. A disabled control is typically
 * not selectable from the user interface and draws with an
 * inactive or "grayed" look.
 *
 * @param enabled the new enabled state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setEnabled (boolean enabled) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	enableWidget (enabled);
	if (!enabled || (isEnabled () && enabled)) {
		propagateChildren (enabled);
	}
}
/**
 * Causes the receiver to have the <em>keyboard focus</em>, 
 * such that all keyboard events will be delivered to it.
 *
 * @return <code>true</code> if the control got focus, and <code>false</code> if it was unable to.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #forceFocus
 */
public boolean setFocus () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	Decorations shell = menuShell ();
	shell.setSavedFocus (this);
	shell.bringToTop ();
	return OS.XmProcessTraversal (handle, OS.XmTRAVERSE_CURRENT);
}
/**
 * Sets the font that the receiver will use to paint textual information
 * to the font specified by the argument, or to the default font for that
 * kind of control if the argument is null.
 *
 * @param font the new font (or null)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setFont (Font font) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	int fontList = 0;
	if (font != null) fontList = font.handle;
	if (fontList == 0) fontList = defaultFont ();
	setFontList (fontList);
}
void setFontList (int fontList) {
	this.fontList = fontList;
	
	/*
	* Feature in Motif.  Setting the font in a widget
	* can cause the widget to automatically resize in
	* the OS.  This behavior is unwanted.  The fix is
	* to force the widget to resize to original size
	* after every font change.
	*/
	int [] argList1 = {OS.XmNwidth, 0, OS.XmNheight, 0, OS.XmNborderWidth, 0};
	OS.XtGetValues (handle, argList1, argList1.length / 2);

	/* Set the font list */
	int fontHandle = fontHandle ();
	int [] argList2 = {OS.XmNfontList, fontList};
	OS.XtSetValues (fontHandle, argList2, argList2.length / 2);

	/* Restore the widget size */
	OS.XtSetValues (handle, argList1, argList1.length / 2);
}
/**
 * Sets the receiver's foreground color to the color specified
 * by the argument, or to the default system color for the control
 * if the argument is null.
 *
 * @param color the new color (or null)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setForeground (Color color) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (color == null) {
		setForegroundPixel (defaultForeground ());
	} else {
		if (color.isDisposed ()) return;
		setForegroundPixel (color.handle.pixel);
	}
}
void setForegroundPixel (int pixel) {
	int [] argList = {OS.XmNforeground, pixel};
	OS.XtSetValues (handle, argList, argList.length / 2);
	int xDisplay = OS.XtDisplay (handle);
	if (xDisplay == 0) return;
	int xWindow = OS.XtWindow (handle);
	if (xWindow == 0) return;
	OS.XClearArea (xDisplay, xWindow, 0, 0, 0, 0, true);
}
void setGrabCursor (int cursor) {
	/*	| window attributes eventMask grabMask |
	handle xtIsWidget ifFalse: [^self].
	(window := handle xtWindow) isNull ifTrue: [^self].
	attributes := OSXWindowAttributesPtr new.
	XDisplay
		xGetWindowAttributes: window
		windowAttributesReturn: attributes.
	grabMask := ((((((((((ButtonPressMask bitOr: ButtonReleaseMask) bitOr:
 		EnterWindowMask) bitOr: LeaveWindowMask) bitOr: PointerMotionMask) bitOr:
		PointerMotionHintMask) bitOr: Button1MotionMask) bitOr: Button2MotionMask) bitOr:
		Button3MotionMask) bitOr: Button4MotionMask) bitOr: Button5MotionMask) bitOr: ButtonMotionMask.
	eventMask := attributes yourEventMask bitAnd: grabMask.
	XDisplay xChangeActivePointerGrab: eventMask cursor: aCursor time: CurrentTime.
	*/
}
void setKeyState (Event event, XKeyEvent xEvent) {
	if (xEvent.keycode != 0) {
		event.time = xEvent.time;
		byte [] buffer1 = new byte [1];
		int [] keysym = new int [1], status = new int [1];
		if (OS.XLookupString (xEvent, buffer1, buffer1.length, keysym, status) == 0) {
			event.keyCode = Display.translateKey (keysym [0] & 0xFFFF);
		} else {
			event.character = (char) buffer1 [0];
		}
		if ((xEvent.state & OS.Mod1Mask) != 0) event.stateMask |= SWT.ALT;
		if ((xEvent.state & OS.ShiftMask) != 0) event.stateMask |= SWT.SHIFT;
		if ((xEvent.state & OS.ControlMask) != 0) event.stateMask |= SWT.CONTROL;
		if ((xEvent.state & OS.Button1Mask) != 0) event.stateMask |= SWT.BUTTON1;
		if ((xEvent.state & OS.Button2Mask) != 0) event.stateMask |= SWT.BUTTON2;
		if ((xEvent.state & OS.Button3Mask) != 0) event.stateMask |= SWT.BUTTON3;
	}	
}
/**
 * Sets the layout data associated with the receiver to the argument.
 * 
 * @param layoutData the new layout data for the receiver.
 * 
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setLayoutData (Object layoutData) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	this.layoutData = layoutData;
}
/**
 * Sets the receiver's location to the point specified by
 * the arguments which are relative to the receiver's
 * parent (or its display if its parent is null).
 *
 * @param x the new x coordinate for the receiver
 * @param y the new y coordinate for the receiver
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setLocation (int x, int y) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	int topHandle = topHandle ();
	int [] argList = {OS.XmNx, 0, OS.XmNy, 0};
	OS.XtGetValues (topHandle, argList, argList.length / 2);
	boolean sameOrigin = (x == (short) argList [1]) && (y == (short) argList [3]);
	if (sameOrigin) return;
	OS.XtMoveWidget (topHandle, x, y);
	if (!sameOrigin) sendEvent (SWT.Move);
}
/**
 * Sets the receiver's location to the point specified by
 * the argument which is relative to the receiver's
 * parent (or its display if its parent is null).
 *
 * @param location the new location for the receiver
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setLocation (Point location) {
	if (location == null) error (SWT.ERROR_NULL_ARGUMENT);
	setLocation (location.x, location.y);
}
/**
 * Sets the receiver's pop up menu to the argument.
 * All controls may optionally have a pop up
 * menu that is displayed when the user requests one for
 * the control. The sequence of key strokes, button presses
 * and/or button releases that are used to request a pop up
 * menu is platform specific.
 *
 * @param menu the new pop up menu
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_MENU_NOT_POP_UP - the menu is not a pop up menu</li>
 *    <li>ERROR_INVALID_PARENT - if the menu is not in the same widget tree</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setMenu (Menu menu) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (menu != null) {
		if ((menu.style & SWT.POP_UP) == 0) {
			error (SWT.ERROR_MENU_NOT_POP_UP);
		}
		if (menu.parent != menuShell ()) {
			error (SWT.ERROR_INVALID_PARENT);
		}
	}
	this.menu = menu;
}

/**
 * Changes the parent of the widget to be the one provided if
 * the underlying operating system supports this feature.
 * Answers <code>true</code> if the parent is successfully changed.
 *
 * @param parent the new parent for the control.
 * @return <code>true</code> if the parent is changed and <code>false</code> otherwise.
 *
 * @exception SWTError <ul>
 *		<li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
 *		<li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
 *	</ul>
 */
public boolean setParent (Composite parent) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return false;
}

/**
 * If the argument is <code>false</code>, causes subsequent drawing
 * operations in the receiver to be ignored. No drawing of any kind
 * can occur in the receiver until the flag is set to true.
 * Graphics operations that occurred while the flag was
 * <code>false</code> are lost. When the flag is set to <code>true</code>,
 * the entire widget is marked as needing to be redrawn.
 * <p>
 * Note: This operation is a hint and may not be supported on some
 * platforms or for some widgets.
 * </p>
 *
 * @param redraw the new redraw state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * 
 * @see #redraw
 * @see #update
 */
public void setRedraw (boolean redraw) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
}
/**
 * Sets the receiver's size to the point specified by the arguments.
 * <p>
 * Note: Attempting to set the width or height of the
 * receiver to a negative number will cause that
 * value to be set to zero instead.
 * </p>
 *
 * @param width the new width for the receiver
 * @param height the new height for the receiver
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setSize (int width, int height) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	/*
	* Feature in Motif.  Motif will not allow a window
	* to have a zero width or zero height.  The fix is
	* to ensure these values are never zero.
	*/
	int topHandle = topHandle ();
	int [] argList = {OS.XmNwidth, 0, OS.XmNheight, 0, OS.XmNborderWidth, 0};
	OS.XtGetValues (topHandle, argList, argList.length / 2);
	int newWidth = Math.max (width - (argList [5] * 2), 1);
	int newHeight = Math.max (height - (argList [5] * 2), 1);
	boolean sameExtent = (newWidth == argList [1]) && (newHeight == argList [3]);
	if (sameExtent) return;
	OS.XtResizeWidget (topHandle, newWidth, newHeight, argList [5]);
	if (!sameExtent) sendEvent (SWT.Resize);
}
/**
 * Sets the receiver's size to the point specified by the argument.
 * <p>
 * Note: Attempting to set the width or height of the
 * receiver to a negative number will cause them to be
 * set to zero instead.
 * </p>
 *
 * @param size the new size for the receiver
 * @param height the new height for the receiver
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setSize (Point size) {
	if (size == null) error (SWT.ERROR_NULL_ARGUMENT);
	setSize (size.x, size.y);
}
/**
 * Sets the receiver's tool tip text to the argument, which
 * may be null indicating that no tool tip text should be shown.
 *
 * @param string the new tool tip text (or null)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setToolTipText (String string) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	toolTipText = string;
}
/**
 * Marks the receiver as visible if the argument is <code>true</code>,
 * and marks it invisible otherwise. 
 * <p>
 * If one of the receiver's ancestors is not visible or some
 * other condition makes the receiver not visible, marking
 * it visible may not actually cause it to be displayed.
 * </p>
 *
 * @param visible the new visibility state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setVisible (boolean visible) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	int topHandle = topHandle ();
	int [] argList = {OS.XmNmappedWhenManaged, 0};
	OS.XtGetValues (topHandle, argList, argList.length / 2);
	if ((argList [1] != 0) == visible) return;
	OS.XtSetMappedWhenManaged (topHandle, visible);
	sendEvent (visible ? SWT.Show : SWT.Hide);
}
void setZOrder (Control control, boolean above) {
	/*
	* Feature in Xt.  We cannot use XtMakeGeometryRequest() to
	* restack widgets because this call can fail under certain
	* conditions.  For example, XtMakeGeometryRequest() answers
	* XtGeometryNo when attempting to bring a child widget that
	* is larger than the parent widget to the front.  The fix
	* is to use X calls instead.
	*/
	int topHandle1 = topHandle ();
	int display = OS.XtDisplay (topHandle1);
	if (display == 0) return;
	if (!OS.XtIsRealized (topHandle1)) {
		Shell shell = this.getShell ();
		shell.realizeWidget ();
	}
	int window1 = OS.XtWindow (topHandle1);
	if (window1 == 0) return;
	if (control == null) {
		if (above) {
			OS.XRaiseWindow (display, window1);
			if (parent != null) parent.moveAbove (topHandle1, 0);
		} else {
			OS.XLowerWindow (display, window1);
			if (parent != null) parent.moveBelow (topHandle1, 0);
		}
		return;
	}
	int topHandle2 = control.topHandle ();
	if (display != OS.XtDisplay (topHandle2)) return;
	if (!OS.XtIsRealized (topHandle2)) {
		Shell shell = control.getShell ();
		shell.realizeWidget ();
	}
	int window2 = OS.XtWindow (topHandle2);
	if (window2 == 0) return;
	XWindowChanges struct = new XWindowChanges ();
	struct.sibling = window2;
	struct.stack_mode = above ? OS.Above : OS.Below;
	/*
	* Feature in X. If the receiver is a top level, XConfigureWindow ()
	* will fail (with a BadMatch error) for top level shells because top
	* level shells are reparented by the window manager and do not share
	* the same X window parent.  This is the correct behavior but it is
	* unexpected.  The fix is to use XReconfigureWMWindow () instead.
	* When the receiver is not a top level shell, XReconfigureWMWindow ()
	* behaves the same as XConfigureWindow ().
	*/
	int screen = OS.XDefaultScreen (display);
	int flags = OS.CWStackMode | OS.CWSibling;
	OS.XReconfigureWMWindow (display, window1, screen, flags, struct);
	if (above) {
		if (parent != null) parent.moveAbove (topHandle1, topHandle2);
	} else {
		if (parent != null) parent.moveBelow (topHandle1, topHandle2);
	}
}
/**
 * Returns a point which is the result of converting the
 * argument, which is specified in display relative coordinates,
 * to coordinates relative to the receiver.
 * <p>
 * @param point the point to be translated (must not be null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Point toControl (Point point) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	short [] root_x = new short [1], root_y = new short [1];
	OS.XtTranslateCoords (handle, (short) 0, (short) 0, root_x, root_y);
	return new Point (point.x - root_x [0], point.y - root_y [0]);
}
/**
 * Returns a point which is the result of converting the
 * argument, which is specified in coordinates relative to
 * the receiver, to display relative coordinates.
 * <p>
 * @param point the point to be translated (must not be null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Point toDisplay (Point point) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	short [] root_x = new short [1], root_y = new short [1];
	OS.XtTranslateCoords (handle, (short) point.x, (short) point.y, root_x, root_y);
	return new Point (root_x [0], root_y [0]);
}
boolean translateMnemonic (int aKey, XKeyEvent xEvent) {
	if (xEvent.state != OS.Mod1Mask) {
		if (xEvent.state != 0 || !(this instanceof Button)) {
			return false;
		}
	}
	Decorations shell = menuShell ();
	if (!shell.isVisible () || !shell.isEnabled ()) return false;
	char ch = mbcsToWcs ((char) aKey);
	return ch != 0 && shell.traverseMnemonic (ch);
}
boolean translateTraversal (int key, XKeyEvent xEvent) {
	int detail = 0;
	switch (key) {
		case OS.XK_Escape:
		case OS.XK_Cancel:
			Shell shell = getShell ();
			if (shell.parent == null) return false;
			if (!shell.isVisible () || !shell.isEnabled ()) return false;
			detail = SWT.TRAVERSE_ESCAPE;
			break;
		case OS.XK_Return:
			Button button = menuShell ().getDefaultButton ();
			if (button == null || button.isDisposed ()) return false;
			if (!button.isVisible () || !button.isEnabled ()) return false;
			detail = SWT.TRAVERSE_RETURN;
			break;
		case OS.XK_Tab:
			detail = SWT.TRAVERSE_TAB_PREVIOUS;
			boolean next = (xEvent.state & OS.ShiftMask) == 0;
			if (next && ((xEvent.state & OS.ControlMask) != 0)) return false;
			if (next) detail = SWT.TRAVERSE_TAB_NEXT;
			break;
		case OS.XK_Up:
		case OS.XK_Left: 
			detail = SWT.TRAVERSE_ARROW_PREVIOUS;
			break;
		case OS.XK_Down:
		case OS.XK_Right:
			detail = SWT.TRAVERSE_ARROW_NEXT;
			break;
		default:
			return false;
	}
	boolean doit = (detail & traversalCode ()) != 0;
	if (hooks (SWT.Traverse)) {
		Event event = new Event();
		event.doit = doit;
		event.detail = detail;
		setKeyState (event, xEvent);
		sendEvent (SWT.Traverse, event);
		doit = event.doit;
		detail = event.detail;
	}
	/*
	* NOTE:  The native widgets handle tab and arrow key traversal
	* so it is not necessary to traverse these keys.  A canvas widget
	* has no native traversal by definition so it is necessary to
	* traverse all keys.
	*/
	if (doit) {
		int flags = SWT.TRAVERSE_RETURN | SWT.TRAVERSE_ESCAPE;
		if ((detail & flags) != 0 || (state & CANVAS) != 0) {
			return traverse (detail);
		}
	}
	return false;
}
int traversalCode () {
	int code = SWT.TRAVERSE_ESCAPE | SWT.TRAVERSE_RETURN;
	int [] argList = {OS.XmNnavigationType, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	if (argList [1] == OS.XmNONE) {
		code |= SWT.TRAVERSE_ARROW_NEXT | SWT.TRAVERSE_ARROW_PREVIOUS;
	} else {
		code |= SWT.TRAVERSE_TAB_NEXT | SWT.TRAVERSE_TAB_PREVIOUS;
	}
	return code;
}
boolean traverseMnemonic (char key) {
	if (!isVisible () || !isEnabled ()) return false;
	return mnemonicMatch (key) && mnemonicHit ();
}
/**
 * Based on the argument, perform one of the expected platform
 * traversal action. The argument should be one of the constants:
 * <code>SWT.TRAVERSE_ESCAPE</code>, <code>SWT.TRAVERSE_RETURN</code>, 
 * <code>SWT.TRAVERSE_TAB_NEXT</code>, <code>SWT.TRAVERSE_TAB_PREVIOUS</code>, 
 * <code>SWT.TRAVERSE_ARROW_NEXT</code> and <code>SWT.TRAVERSE_ARROW_PREVIOUS</code>.
 *
 * @param traversal the type of traversal
 * @return true if the traversal succeeded
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean traverse (int traversal) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (!isFocusControl () && !setFocus ()) return false;
	switch (traversal) {
		case SWT.TRAVERSE_ESCAPE:		return traverseEscape ();
		case SWT.TRAVERSE_RETURN:		return traverseReturn ();
		case SWT.TRAVERSE_TAB_NEXT:		return traverseGroup (true);
		case SWT.TRAVERSE_TAB_PREVIOUS:		return traverseGroup (false);
		case SWT.TRAVERSE_ARROW_NEXT:		return traverseItem (true);
		case SWT.TRAVERSE_ARROW_PREVIOUS:	return traverseItem (false);	
	}
	return false;
}
boolean traverseEscape () {
	Shell shell = getShell ();
	if (shell.parent == null) return false;
	if (!shell.isVisible () || !shell.isEnabled ()) return false;
	shell.close ();
	return true;
}
boolean traverseGroup (boolean next) {
	return OS.XmProcessTraversal (handle, next ? OS.XmTRAVERSE_NEXT_TAB_GROUP : OS.XmTRAVERSE_PREV_TAB_GROUP);
}
boolean traverseItem (boolean next) {
	return OS.XmProcessTraversal (handle, next ? OS.XmTRAVERSE_NEXT : OS.XmTRAVERSE_PREV);
}
boolean traverseReturn () {
	Button button = menuShell ().getDefaultButton ();
	if (button == null || button.isDisposed ()) return false;
	if (!button.isVisible () || !button.isEnabled ()) return false;
	button.click ();
	return true;
}
/**
 * Forces all outstanding paint requests for the widget tree
 * to be processed before this method returns.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #redraw
 */
public void update () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	int display = OS.XtDisplay (handle);
	if (display == 0) return;
	int window = OS.XtWindow (handle);
	if (window == 0) return;
	XAnyEvent event = new XAnyEvent ();
	OS.XSync (display, false);  OS.XSync (display, false);
	while (OS.XCheckWindowEvent (display, window, OS.ExposureMask, event)) {
		OS.XtDispatchEvent (event);
	}
}
int processMouseHover (int id) {
	Display display = getDisplay();
	Event event = new Event();
	Point local = toControl(display.getCursorLocation());
	event.x = local.x; event.y = local.y;
	postEvent (SWT.MouseHover, event);
	display.showToolTip(handle, toolTipText);
	return 0;
}
}