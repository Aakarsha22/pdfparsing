package pdftohtml;

/*
* ==============================
* PDF to HTML Conversion Project
* ==============================
*
* Project Info:  http://tamirhassan.com/project/
* Project Lead:  Tamir Hassan, University of Warwick, UK
*
* Submitted as CS310 Third Year Project
*
* ----------------
* PdfGrouping.java
* ----------------
*
* Original Author:  Tamir Hassan (project@tamirhassan.com)
*
* Last Modified: 30 April 2003
* --------------------------
*/

import java.awt.image.*;
import java.io.*;
import org.jpedal.*;
import org.jpedal.gui.*;
import org.jpedal.io.*;
import org.jpedal.objects.*;
import org.jpedal.utils.*;
import org.jpedal.grouping.*;

import org.jdom.*;
import org.jdom.output.XMLOutputter;
import java.util.*;

/**
 * This class is designed to extend the PdfGenericGrouping class in
 * JPedal to provide routines for merging the extracted text into
 * paragraphs and columns.<P>
 *
 * The method processPageFragments interfaces with the front end 
 * and with the JPedal libraries copyToFragmentArrays and
 * readFromFragmentArrays.<P>
 *
 * In between these stages the text is grouped into columns (if the
 * -c option has been used) and merged to form continuous paragraphs
 */

public class PdfGrouping extends PdfGenericGrouping
{

	/* set global variables */

	/** array to hold the order of each text fragment for sorting */
	public Integer[] order;

	/** group (ie column) fragment belongs to */
	public int[] group;

	/** returns TRUE if text fragment is over threshold size */
	public boolean[] isHeading;

	/** returns TRUE if font includes "Bold" as substring */
	public boolean[] isBold;

	/** returns TRUE if font includes "Italic" as substring */
	public boolean[] isItalic;

	/** total number of text fragments */
	public int total_fragments;

	/** number of fragments actually used during merging */
	public int used_fragments;

	/** used to store the number of used fragments for writing back to arrays */
	public int tempvar;

	/** variable to keep track of the left margin of text */
	public int[] leftborder;

	/** variable to keep track of the right margin of text */
	public int[] rightborder;

	/** variable to keep track of text width */
	public float text_width;

	//////////////////////////////////////////////////////////////////////
	/**
	 * returns plain text from content attribute
	 * (ie strips XML metadata)
	 */
	public String textOf(String content)
	{
	  int start = content.indexOf(">",9) + 1;
	  int end = content.lastIndexOf("<",content.length()-7);

	  return content.substring(start,end);
	}
	//////////////////////////////////////////////////////////////////////
	/**
	 * returns TRUE if text in content attribute is italic
	 */
	public boolean isItalic(String content)
	{
		int styleindex = content.indexOf("style=", 9);
		int faceindex = content.indexOf("face=", 5);
		int start = content.indexOf("face=", 5) + 5;
		int end = styleindex - 2;

		String font = content.substring(start, end);

		if (font.indexOf("Italic") == -1) return false;
			else return true;
	}
	//////////////////////////////////////////////////////////////////////
	/**
	 * returns TRUE if text in content attribute is bold
	 */
	public boolean isBold(String content)
	{
		int styleindex = content.indexOf("style=", 9);
		int faceindex = content.indexOf("face=", 5);
		int start = content.indexOf("face=", 5) + 5;
		int end = styleindex - 2;

		String font = content.substring(start, end);

		if (font.indexOf("Bold") == -1) return false;
			else return true;
	}
	//////////////////////////////////////////////////////////////////////
	/**
	 * returns TRUE if text in content attribute is empty or a string of
	 * spaces
	 */
	public boolean isEmpty(String content)
	{
		for (int count = 0; count < content.length(); count ++ )
	  	{
			if ((int)content.charAt(count) > 32)
			{
				return false;
			}
		}
		return true;
	}
	//////////////////////////////////////////////////////////////////////
	/**
	 * returns the line spacing of the line at the given UNSORTED position
	 */
	public float findLineSpacing(int unsorted_pos)
	{
		int sorted_pos, next_pos;
		float space1, space2;
		int frag = unsorted_pos;
		float spacing;

		/* find line height if cannot detect paragraph */
		sorted_pos = order[frag].intValue();
		float lineheight = heights[sorted_pos];

		/* if we have reached the last text fragment return line height as spacing */
		if ((frag + 1) >= used_fragments) return lineheight;

		/* if font sizes are different return line height as spacing */
		next_pos = order[frag + 1].intValue();
		if (f_end_font_size[sorted_pos] != f_start_font_size[next_pos]) return lineheight;

		do
		{
			/* if we have reached the last text fragment return line height as spacing */
			if ((frag + 1) >= used_fragments) return lineheight;

			sorted_pos = order[frag].intValue();

			frag ++;
			next_pos = order[frag].intValue();
		}
		while (sameLine (next_pos, sorted_pos) == true);

		space1 = f_y1[sorted_pos] - f_y1[next_pos];

		do
		{
			/* if we have reached the last text fragment return line height as spacing */
			if ((frag + 1) >= used_fragments) return heights[unsorted_pos];

			sorted_pos = order[frag].intValue();

			frag ++;
			next_pos = order[frag].intValue();
		}
		while (sameLine (next_pos, sorted_pos) == true);

		space2 = f_y1[sorted_pos] - f_y1[next_pos];

		if (space1 > space2) spacing = space2; else spacing = space1;

		sorted_pos = order[unsorted_pos].intValue();

		if ((spacing > lineheight * 2.5) || (spacing < lineheight * 0.5)) return lineheight;
		else return spacing;
	}
	//////////////////////////////////////////////////////////////////////
	/**
	 * detects whether text in two positions is on the same line
	 */
	public boolean sameLine(int sorted_pos, int prev_pos)
	{
		float verterr = heights[sorted_pos] / 4;

		boolean y1_same = (f_y1[sorted_pos] >= (f_y1[prev_pos] - verterr)) &&
                        (f_y1[sorted_pos] <= (f_y1[prev_pos] + verterr));

		boolean y2_same = (f_y2[sorted_pos] >= (f_y2[prev_pos] - verterr)) &&
                        (f_y2[sorted_pos] <= (f_y2[prev_pos] + verterr));

		boolean y1_current = ((f_y1[sorted_pos] >= f_y2[prev_pos]) &&
                           (f_y1[sorted_pos] <= f_y1[prev_pos])) ||
                           ((f_y1[sorted_pos] <= f_y2[prev_pos]) &&
                           (f_y1[sorted_pos] >= f_y1[prev_pos]));

		boolean y2_current = ((f_y2[sorted_pos] <= f_y1[prev_pos]) &&
                           (f_y2[sorted_pos] >= f_y2[prev_pos])) ||
                           ((f_y2[sorted_pos] >= f_y1[prev_pos]) &&
                           (f_y2[sorted_pos] <= f_y2[prev_pos]));

		boolean y1_previous = ((f_y1[prev_pos] >= f_y2[sorted_pos]) &&
                            (f_y1[prev_pos] <= f_y1[sorted_pos])) ||
                            ((f_y1[prev_pos] <= f_y2[sorted_pos]) &&
                            (f_y1[prev_pos] >= f_y1[sorted_pos]));

		boolean y2_previous = ((f_y2[prev_pos] <= f_y1[sorted_pos]) &&
                            (f_y2[prev_pos] >= f_y2[sorted_pos])) ||
                            ((f_y2[prev_pos] >= f_y1[sorted_pos]) &&
                            (f_y2[prev_pos] <= f_y2[sorted_pos]));

		return y1_current || y2_current || y1_previous || y2_previous;
	}
	//////////////////////////////////////////////////////////////////////
	/**
	 * detects whether text blocks are directly horizontally adjacent
	 */
	public boolean nextChar(int sorted_pos, int prev_pos)
	{
		/* horizontal error margin 25pc */
		float horizerr = heights[sorted_pos] / 4;

		return ((f_x1[sorted_pos] > (f_x2[prev_pos] - horizerr)) &&
		(f_x1[sorted_pos] < (f_x2[prev_pos] + horizerr)));
	}
	public boolean changeLineSpacing(int sorted_pos, int prev_pos, float linespacing)
	{
		float verterr = (float) (linespacing * 0.70);
		boolean too_close = (f_y1[sorted_pos] > (f_y1[prev_pos] - verterr));
		boolean below = (f_y1[sorted_pos] < f_y1[prev_pos]);
		return too_close && below;
	}
	//////////////////////////////////////////////////////////////////////
	/**
	 * detects whether text blocks are directly vertically adjacent
	 */
	public boolean nextLine(int sorted_pos, int prev_pos, float linespacing)
	{
		/* if font sizes are different return false (i.e. next paragraph) */
		if (f_start_font_size[sorted_pos] != f_end_font_size[prev_pos]) return false;

		/* set vertical error margin */
		float verterr = (float) (linespacing * 1.27);

		boolean aboveThreshold = (f_y1[sorted_pos] > (f_y1[prev_pos] - verterr));
		boolean belowprev = (f_y1[sorted_pos] < f_y1[prev_pos]);

		return aboveThreshold && belowprev;
	}
	//////////////////////////////////////////////////////////////////////
	/**
	 * determines whether text blocks are hyphenated; if so, removes the
	 * hyphen and returns TRUE
	 */
	public boolean isHyphenated(int sorted_pos, int prev_pos)
	{
		/* return false if text fragments are different sizes */
		if (f_start_font_size[sorted_pos] != f_end_font_size[prev_pos]) return false;

		int length = contents[prev_pos].length();
		String prev_string = contents[prev_pos];
		String current_string = contents[sorted_pos];

		/* avoid exception if called with incorrect values*/
		if (length < 2) return false;

		/* necessary to assign characters due to bug in Java */
		char lastchar = prev_string.charAt(length -1);
		char penulchar = prev_string.charAt(length -2);

		/* if last character is a hyphen and is preceded by a letter */
		/* (uppercase or lowercase) and next pos begins with letter */
		if ((lastchar == 45) &&
		    (penulchar >= 65) &&
		    (penulchar <= 122) &&
		    (penulchar >= 65) &&
		    (penulchar <= 122))
		{
			/** remove hyphen from string */
			contents[prev_pos] = prev_string.substring(0, (length - 1));
			return true;
		}
		else return false;
	}

	//////////////////////////////
	/** COMPARATORS FOR SORTING */
	//////////////////////////////

	//////////////////////////////////////////////////////////////////////
	/**
	 * sorts in order of y then x taking similar values of y as being the
	 * same
	 */
	class YErrComparator implements Comparator
	{
		public int compare(Object o1, Object o2)
		{
		   Integer obj1 = (Integer)o1, obj2 = (Integer)o2;
		   int current_pos = obj1.intValue();
		   int prev_pos = obj2.intValue();

			float c_x1 = f_x1[current_pos], c_y1 = f_y1[current_pos];
			float p_x1 = f_x1[prev_pos], p_y1 = f_y1[prev_pos];

			if (sameLine(current_pos, prev_pos))
			{
				return (int) (c_x1 - p_x1);
			}
			else
			{
				return (int) (p_y1 - c_y1);
			}
		}

		public boolean equals(Object obj)
		{
			return obj.equals (this);
		}
	}
	//////////////////////////////////////////////////////////////////////
	/**
	 * sorts in order of y then x
	 */
	class XYComparator implements Comparator
	{
	  public int compare(Object o1, Object o2)
	  {
	    Integer obj1 = (Integer)o1, obj2 = (Integer)o2;
	    float x1 = f_x1[(int) obj1.intValue()], y1 = f_y1[(int) obj1.intValue()];
	    float x2 = f_x1[(int) obj2.intValue()], y2 = f_y1[(int) obj2.intValue()];
	    	if (y1 == y2)
	    	{
	      		return (int) (x1 - x2);
	    	}
	    	else
	    	{
	      		return (int) (y2 - y1);
	    	}
	  }

	  public boolean equals(Object obj)
	  {
	    return obj.equals (this);
	  }
	}
	//////////////////////////////////////////////////////////////////////
	/**
	 * sorts in order of group (stable sort)
	 */
	class GroupComparator implements Comparator
	{
	  public int compare(Object o1, Object o2)
	  {
	    Integer obj1 = (Integer)o1, obj2 = (Integer)o2;
	    float g1 = group[(int) obj1.intValue()];
	    float g2 = group[(int) obj2.intValue()];

	    return (int) (g1 - g2);
          }

	  public boolean equals(Object obj)
	  {
	    return obj.equals (this);
	  }
	}
	//////////////////////////////////////////////////////////////////////
	/**
	 * sorts gaps in order of x2 co-ordinate
	 */
	class GapComparator implements Comparator
	{
	  public int compare(Object o1, Object o2)
	  {
	    float val1 = (float)((rectangle)o1).getx2(), val2 = (float)((rectangle)o2).getx2();

	    return (int) (val1 - val2);
          }

	  public boolean equals(Object obj)
	  {
	    return obj.equals (this);
	  }
	}

	//////////////////////////////////////////////////////////////////////
	/**
	 * rectangle class: each object has four co-ordinates (x1, x2, y1, y2)
	 *
	 * It was decided not to use java.awt.Rectangle as it only provides
	 * direct access to the x1 and y1 co-ordinates
	 *
	 * Methods are included to check for intersection with text blocks
	 * and for changing the size ("growing") the rectangle
	 */
	class rectangle
	{
		/* co-ordinates of rectangle */
		private float x1, x2, y1, y2;

		/* flag to indicate whether gap has been intersected */
		/* and should not be reused */
		private boolean clashed;

		/**
	 	* constructor method
	 	*/
		public rectangle(float f_x1, float f_x2, float f_y1, float f_y2)
		{
			x1 = f_x1; x2 = f_x2; y1 = f_y1; y2 = f_y2;
			clashed = false;
		}
		/**
	 	* returns x1 co-ordinate
	 	*/
		public float getx1()
		{
			return x1;
		}
		/**
	 	* returns x2 co-ordinate
	 	*/
		public float getx2()
		{
			return x2;
		}
		/**
	 	* returns y1 co-ordinate
	 	*/
		public float gety1()
		{
			return y1;
		}
		/**
	 	* returns y2 co-ordinate
	 	*/
		public float gety2()
		{
		 	return y2;
		}
		/**
	 	* grows the size of the rectangle to accommodate the given co-ordinates
	 	*/
		public void growToAccommodate(float c_x1, float c_x2, float c_y1, float c_y2)
		{
			if (c_x1 < x1) x1 = c_x1;
			if (c_x2 > x2) x2 = c_x2;
			if (c_y1 > y1) y1 = c_y1;
			if (c_y2 < y2) y2 = c_y2;
		}

		//////////////////////////////////////////////////////////////////////
		/**
	 	* takes co-ordinates of two (successive) text fragments and
		* adjusts co-ordinates of current gap as appropriate
	 	*/
		public boolean checkAndUpdate(float c_x1, float c_x2, float c_y1, float c_y2, float p_x1, float p_x2, float p_y1, float p_y2)
		{
			/* check if there is a clash */
			if (clashed == false)
			{
				if ((c_x1 > x1) || (c_x2 < x2)) // current text block doesn't clash in gap
				{
					if (c_y2 < y2) y2 = c_y2; // grow gap down
				}
				else
				{
					//clash; end block at y1
					clashed = true;
					// y2 = c_y1; artificially tall!
				}

				// check if there is a gap
				if (c_x1 > (p_x2 + 4.0))
				{
					// and gap belongs to this rectangle
					if ((p_x2 <= x1) && (c_x1 >= x2))
					{
						// no trimming necessary
						return true;
					}
					else if ((p_x2 > x1) && (p_x2 < x2) && (c_x1 <= x2) && (c_x1 >= x1))
					{
						// trim from both sides
						x1 = p_x2; x2 = c_x1;
						return true;
					}
					else if ((p_x2 > x1) && (p_x2 < x2) && (c_x1 >= x2))
					{
						// trim from left
						x1 = p_x2;
						return true;
					}
					else if ((p_x2 < x1) && (c_x1 < x2) && (c_x1 >= x1))
					{
						// trim from right
						x2 = c_x1;
						return true;
					}
					else
					{
						// gap does not match rectangle
						return false;
					}
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}
	}

	//////////////////////////////////////////////////////////////////////
	/**
	* outputs the distance between the left edge of the leftmost text co-ordinate
	* and the right edge of the rightmost text co-ordinate
	*/
	public float findTextWidth()
	{
		float leftmost_x1 = -1;
		float rightmost_x2 = -1;

		/* work through each text fragment finding leftmost and rightmost co-ordinates */
		for (int frag = 0; frag < total_fragments; frag ++)
		{
			/* if fragment is not empty */
			if (isEmpty(textOf(contents[frag])) == false)
			{
				if (leftmost_x1 > 0)
				{
					if (f_x1[frag] < leftmost_x1) leftmost_x1 = f_x1[frag];
				}
				else
				{
					leftmost_x1 = f_x1[frag];
				}
				if (rightmost_x2 > 0)
				{
					if (f_x2[frag] > rightmost_x2) rightmost_x2 = f_x2[frag];
				}
				else
				{
					rightmost_x2 = f_x2[frag];
				}
			}
		}
		return (rightmost_x2 - leftmost_x1);
	}

	//////////////////////////////////////////////////////////////////////
	/**
	* checks whether the current text fragment is miscellaneous
	* ie not surrounded by other fragments
	*/
	public boolean isMiscellaneous(int current_frag)
	{
		int sorted_pos = order[current_frag].intValue();
		int test_pos;

		/* return TRUE for vertical text */
		if (f_is_horizontal[sorted_pos] == false) return true;

		/* return FALSE for any "heading" (text over 24 pt) */
		if (isHeading[sorted_pos]) return false;

		if (current_frag > 0)
		{
			/* search back through fragments (as they are already in order) */
			for (int frag = current_frag - 1; frag >= 0; frag --)
			{
				test_pos = order[frag].intValue();

				if (((f_x2[test_pos] >= (f_x1[sorted_pos] - 24)) &&
				     (f_x1[test_pos] <= (f_x2[sorted_pos] + 24))) &&
				    ((f_y2[test_pos] <= (f_y1[sorted_pos] + 24)) &&
				     (f_y1[test_pos] >= (f_y2[sorted_pos] - 24))))
				{
					return false;
				}
			}
		}

		if (current_frag < (used_fragments - 1))
		{
			/* search forward through fragments */
			for (int frag = current_frag + 1; frag <= (used_fragments - 1); frag ++)
			{
				test_pos = order[frag].intValue();

				if (((f_x2[test_pos] >= (f_x1[sorted_pos] - 24)) &&
				     (f_x1[test_pos] <= (f_x2[sorted_pos] + 24))) &&
				    ((f_y2[test_pos] <= (f_y1[sorted_pos] + 24)) &&
				     (f_y1[test_pos] >= (f_y2[sorted_pos] - 24))))
				{
					return false;
				}
			}
		}

		return true;
	}
	//////////////////////////////////////////////////////////////////////
	/**
	* goes through each text fragment removing embedded style information, 
	* setting isHeading if over the threshold size and setting isBold and
	* isItalic as appropriate; detects dropped capitals and ensures that
	* they are not classified as headings.
	*/
	public void findHeadings()
	{
		/* initialize new variables */
		isHeading = new boolean[total_fragments];
		isBold = new boolean[total_fragments];
		isItalic = new boolean[total_fragments];
		int sorted_pos, next_pos;

		for (int frag = 0; frag < used_fragments; frag ++)
		{
			sorted_pos = order[frag].intValue();
			if (isItalic(contents[sorted_pos])) isItalic[sorted_pos] = true; else isItalic[sorted_pos] = false;
			if (isBold(contents[sorted_pos])) isBold[sorted_pos] = true; else isBold[sorted_pos] = false;

			if ((f_start_font_size[sorted_pos] > 14) || (f_end_font_size[sorted_pos] > 14))
			{
				isHeading[sorted_pos] = true;
				contents[sorted_pos] = textOf(contents[sorted_pos]);
				if (((contents[sorted_pos]).length() == 1) && (frag < (total_fragments - 1)))
				{
					next_pos = order[frag + 1].intValue();
					if ((sameLine(next_pos, sorted_pos))) isHeading[sorted_pos] = false;
				}
			}
			else
			{
				isHeading[sorted_pos] = false;
				contents[sorted_pos] = textOf(contents[sorted_pos]);
			}
		}
	}
	//////////////////////////////////////////////////////////////////////
	/**
	* analyses the text fragments in order and creates rectangle
	* objects for gaps between columns; 
	* precondition: arrays must be sorted in Y-then-X order
	*/
	public Object[] findColumnGaps()
	{
		/* create vector to store column gaps as rectangle objects */
		Vector gaps = new Vector(); // replace with vector!

		/* initialize variables */
		int counter = 0; 		// counter of last rectangle added
		int sorted_pos, prev_pos;	// positions of text fragments in arrays
		float x1, x2, y1, y2;		// co-ordinates of gap
		boolean merged = false; 	// flag to break out of loop

		/* go through each text fragment in order */
		for (int frag = 0; frag < used_fragments; frag ++)
		{
			/* assign sorted_pos and prev_pos */
			sorted_pos = order[frag].intValue();
			if (frag > 0) prev_pos = order[frag - 1].intValue(); else prev_pos = 0;

			/* get co-ordinates of gap between text fragments */
			float c_x1 = f_x1[sorted_pos]; float c_x2 = f_x2[sorted_pos];
			float c_y1 = f_y1[sorted_pos]; float c_y2 = f_y2[sorted_pos];
			float p_x1 = f_x1[prev_pos]; float p_x2 = f_x2[prev_pos];
			float p_y1 = f_y1[prev_pos]; float p_y2 = f_y2[prev_pos];

			/* clear flag to break out of loop */
			merged = false;

			/* go through each rectangle to check if it fits with, or is crossed by, */
			/* the gap between the current and previous text fragments */
			for (int gap = 0; gap < counter; gap ++)
			{
				if (frag > 0)
				{
					if (((rectangle)gaps.elementAt(gap)).checkAndUpdate(c_x1, c_x2, c_y1, c_y2, p_x1, p_x2,
					p_y1, p_y2) == true)
					{
						merged = true;
						/* no need to create new rectangle as existing rectangle */
						/* has been grown to accommodate the gap between the text fragments */
					}
				}
			}

			/* if still not merged then create a new rectangle */
			if (merged == false)
			{
				// create new horizontal gap
				if (c_x1 >= (p_x2 + 4.0))
				{
					if (c_y1 > p_y1) y1 = c_y1; else y1 = p_y1;
					if (c_y2 < p_y2) y2 = c_y2; else y2 = p_y2;
					x1 = p_x2; x2 = c_x1;
					gaps.addElement(new rectangle(x1, x2, y1, y2));
					counter ++;
				}
			}
		}

		/** remove gaps less than 36 points tall and 4 points wide */

		for (int i = 0; i < gaps.size(); i ++)
		{
			rectangle current_gap = (rectangle)(gaps.elementAt(i));
			if (((current_gap.gety1() -  current_gap.gety2()) < 36.0) || ((current_gap.getx2() -  current_gap.getx1()) < 4.0))
			{
				gaps.removeElementAt(i);
			}
		}

		Object[] arraygaps = gaps.toArray();
		Arrays.sort(arraygaps, new GapComparator());

		return arraygaps;
	}
	//////////////////////////////////////////////////////////////////////
	/**
	* assigns a group to each text fragment based on its location
	* relative to the column gaps
	*/
	public void groupTextElements(Object[] gaps)
	{
		int sorted_pos;
		int no_of_gaps = gaps.length;

		/* initialize group array */
		group = new int[total_fragments];

		/* initialize variables for text fragment (c_xx) and rectangle (r_xx) co-ordinates */
		float c_x1, c_x2, c_y1, c_y2, r_x1, r_x2, r_y1, r_y2;

		/* assign all fragments to group 0 to begin with */
		/* this will ensure that even unused fragments have a group */
		for (int frag = 0; frag < used_fragments; frag ++)
		{
			group[frag] = 0;
		}

		/* work through each column gap assigning group numbers */
		for (int groupno = 0; groupno < no_of_gaps; groupno ++)
		{
			/* find rightmost x position of current gap (rectangle)*/
			float right = ((rectangle)(gaps[groupno])).getx2();
			float top = ((rectangle)(gaps[groupno])).gety1();

			/* work through each text fragment */
			for (int frag = 0; frag < used_fragments; frag ++)
			{
				sorted_pos = order[frag].intValue();

				/* if fragment is to the right of the gap */
				if (f_x1[sorted_pos] >= right)
				{
					/* assign (or re-assign) group number) */
					group[sorted_pos] = groupno + 1;
				}
			}
		}
	}
	//////////////////////////////////////////////////////////////////////
	/**
	* performs the main merging routine after all the fragments
	* have been ordered and processed
	*/
	public void mergeTextObjects(boolean detect_columns)
	{
		float linespacing;
		int sorted_pos, prev_pos;
		float left_margin = f_x1[order[0].intValue()];
		float right_margin = f_x2[order[0].intValue()];
		float indent_guard = f_x1[order[0].intValue()];
		String style;

		Properties props = System.getProperties();
		String newLine = props.getProperty("line.separator");

		/* first text fragment */
		sorted_pos = order[0].intValue();
		linespacing = findLineSpacing(0);
		String text = contents[sorted_pos];
		if (isHeading[sorted_pos]) contents[sorted_pos] = "<h1>" + newLine; else contents[sorted_pos] = "<p>" + newLine;
		if (isBold[sorted_pos])	contents[sorted_pos] = contents[sorted_pos] + "<b>";
		if (isItalic[sorted_pos]) contents[sorted_pos] = contents[sorted_pos] + "<i>";
		contents[sorted_pos] = contents[sorted_pos] + text;

		for (int frag = 1; frag < order.length; frag ++)
		{
			sorted_pos = order[frag].intValue();
			prev_pos = order[frag - 1].intValue();
			if (isHeading[sorted_pos]) style = "h1"; else style = "p";

			/* if new group or different sized text then find new margins and line spacing */
			if ((group[sorted_pos] != group[prev_pos]) || (f_end_font_size[prev_pos] != f_start_font_size[sorted_pos]))
			{
				left_margin = f_x1[sorted_pos];
				right_margin = f_x2[sorted_pos];
				indent_guard = f_x1[sorted_pos];
				linespacing = findLineSpacing(frag);
			}

			/* include formatting changes where they occur */
			if (isBold[sorted_pos] && !isBold[prev_pos]) contents[prev_pos] = contents[prev_pos] + "<b>";
			if (!isBold[sorted_pos] && isBold[prev_pos]) contents[prev_pos] = contents[prev_pos] + "</b>";
			if (isItalic[sorted_pos] && !isItalic[prev_pos]) contents[prev_pos] = contents[prev_pos] + "<i>";
			if (!isItalic[sorted_pos] && isItalic[prev_pos]) contents[prev_pos] = contents[prev_pos] + "</i>";

			/* if change of style simply concatenate as browser will add its line spacing */

			/* body text to heading */
			if (isHeading[sorted_pos] && !isHeading[prev_pos])
			{
				contents[sorted_pos] = contents[prev_pos] + "</p>" + newLine + "<h1>" + contents[sorted_pos];
				isUsed[prev_pos] = true;
				if (f_x2[sorted_pos] > right_margin) right_margin = f_x2[sorted_pos];
				if (f_x1[sorted_pos] < left_margin) left_margin = f_x1[sorted_pos];
			}
			else
			/* heading to body text */
			if (!isHeading[sorted_pos] && isHeading[prev_pos])
			{
				contents[sorted_pos] = contents[prev_pos] + "</h1>" + newLine + "<p>" + contents[sorted_pos];
				isUsed[prev_pos] = true;
				if (f_x2[sorted_pos] > right_margin) right_margin = f_x2[sorted_pos];
				if (f_x1[sorted_pos] < left_margin) left_margin = f_x1[sorted_pos];
			}
			else
			if (sameLine(sorted_pos, prev_pos))
			/* same line i.e. y1~=y1 and y2~=y2; possibly extend statement
			to look at both y1 and y2 values? */
			{
				if (nextChar(sorted_pos, prev_pos))

				/** next character i.e. x1[sorted_pos]~=x2[prev_pos] */
				{
					contents[sorted_pos] = contents[prev_pos] + contents[sorted_pos];
					isUsed[prev_pos] = true;
					if (f_x2[sorted_pos] > right_margin) right_margin = f_x2[sorted_pos];
				}
				else
				/** next word */
				{
					contents[sorted_pos] = contents[prev_pos] + " " + newLine + contents[sorted_pos];
					isUsed[prev_pos] = true;
					if (f_x2[sorted_pos] > right_margin) right_margin = f_x2[sorted_pos];
				}
				/* if font size changes (e.g. dropped capital) move indent guard right */
				if (f_start_font_size[sorted_pos] != f_start_font_size[prev_pos])
				{
					indent_guard = f_x1[sorted_pos];
				}
			}
			else
			{
				if (changeLineSpacing(sorted_pos, prev_pos, linespacing)) linespacing = findLineSpacing(frag);

				if (nextLine(sorted_pos, prev_pos, linespacing))
				/* next line (i.e. directly under) in pdf */
				{
					if (f_start_font_size[sorted_pos] == f_end_font_size[prev_pos])
					{
						/** next word in paragraph or hyphenated text */

						if (isHyphenated(sorted_pos, prev_pos))
						{
							/** hyphen already removed by isHyphenated method */
							contents[sorted_pos] = contents[prev_pos] + contents[sorted_pos];
							isUsed[prev_pos] = true;
						}
						else
						/* next word in paragraph */
						if ((detect_columns == true && ((f_x2[prev_pos] - left_margin)
						< ((right_margin - left_margin) * 0.6)))
						|| (detect_columns == false && ((f_x2[prev_pos] - left_margin)
						< (text_width * 0.7))))
						{
							if (f_x1[sorted_pos] > indent_guard + 6)
							{
								contents[sorted_pos] = contents[prev_pos] +
								"<BR> &nbsp&nbsp&nbsp&nbsp" + newLine + contents[sorted_pos];
							}
							else
							{
								contents[sorted_pos] = contents[prev_pos] +
								"<BR>" + newLine + contents[sorted_pos];
							}
							isUsed[prev_pos] = true;
						}
						else
						if (f_x1[sorted_pos] > indent_guard + 6) //indent
						{
							contents[sorted_pos] = contents[prev_pos] +
							"<BR> &nbsp&nbsp&nbsp&nbsp" + newLine + contents[sorted_pos];
							isUsed[prev_pos] = true;
						}
						else
						{
							contents[sorted_pos] = contents[prev_pos] + " " +
							newLine + contents[sorted_pos];
							isUsed[prev_pos] = true;
						}
					}
					else
					{
						/* next line */
						contents[sorted_pos] = contents[prev_pos] + "<BR>" + newLine +
						contents[sorted_pos];
						isUsed[prev_pos] = true;
					}
					if (f_x1[sorted_pos] < left_margin)
					{
						left_margin = f_x1[sorted_pos];
						indent_guard = f_x1[sorted_pos];
					}
					if (f_x2[sorted_pos] > right_margin) right_margin = f_x2[sorted_pos];
				}
				else
				/* next paragraph */
				{
					linespacing = findLineSpacing(frag);
					contents[sorted_pos] = contents[prev_pos] + "</" + style + ">" + newLine + "<" + style +
					">" + newLine + contents[sorted_pos];
					isUsed[prev_pos] = true;

					/* adjust margins */
					left_margin = f_x1[sorted_pos];
					if (f_x2[sorted_pos] > right_margin) right_margin = f_x2[sorted_pos];
					indent_guard = f_x1[sorted_pos];
				}
			}
		}
		/* add final </b> or </i> if necessary */
		if (isBold[sorted_pos]) contents[sorted_pos] = contents[sorted_pos] + "</b>" + newLine;
		if (isItalic[sorted_pos]) contents[sorted_pos] = contents[sorted_pos] + "</i>" + newLine;

		/* add final </p> or </h1> */
		if (isHeading[sorted_pos]) contents[sorted_pos] = contents[sorted_pos] + "</h1>" + newLine;
		else contents[sorted_pos] = contents[sorted_pos] + "</p>" + newLine;
	}
	//////////////////////////////////////////////////////////////////////
	/**
	* the main method that is called by the front end;
	* detect_columns is true if the -c option is used
	*/
        // This method is adapted from decodePageFragments
	// in JPedal by IDR Solutions.  It is copyright
	// IDR Solutions and licensed under the LGPL

	final public void processPageFragments( PdfData pdf_data, int page_number, boolean detect_columns )
	{
		this.pdf_data = pdf_data;

		/* init store for data */
		copyDataToFragmentArrays();

		total_fragments = isUsed.length;
		used_fragments = isUsed.length;

		/* removed the embedded widths (set by flag in PdfObjects) */
		removeEncoding();

		/* start an array order to count 1,2,3,4,5... for sorting */
		Integer[] temparray = new Integer[used_fragments];

		/* adjust used_fragments to exclude empty text strings */
		int i = 0;

		for (int pointer = 0; pointer < used_fragments; pointer ++)
		{
			if (isEmpty(textOf(contents[pointer])) == false)
			{
				temparray[i] = new Integer(pointer);
				isUsed[i] = false;
				i ++;
			}
			else
			{
				isUsed[i] = true;
			}
		}

		used_fragments = i;

		/* break out of method if page contains no textual data */
		if (used_fragments == 0)
		{
			System.out.println("No textual data on page");
			return;
		}

		/* order becomes the truncated array */
		order = new Integer[used_fragments];
		System.arraycopy(temparray, 0, order, 0, used_fragments);

		text_width = findTextWidth();

		/* sort fragments into y then x order */
		Arrays.sort(order, new XYComparator());

		/* find headings and formatting information in all text fragments */
		findHeadings();

		/* find column gaps */
		Object[] gaps = findColumnGaps(); 

		/* group text fragments by column gap if column detection turned on */
		if (detect_columns)
		{
			groupTextElements(gaps);
		}
		else
		{
			group = new int[total_fragments];
			for (int frag = 0; frag < total_fragments; frag ++)
			{
				group[frag] = 0;
			}
		}

		/* put all miscellanous items into the last group */
		for (int frag = 0; frag < used_fragments; frag ++)
		{
			if (isMiscellaneous(frag))
			{
				group[order[frag].intValue()] = gaps.length + 1;
			}
		}

		/* sort into y then x order allowing a margin for "error" */
		Arrays.sort(order, new YErrComparator());

		/* sort into group order (stable sort) */
		Arrays.sort(order, new GroupComparator());

		/* perform the merging of the text objects */
		mergeTextObjects(detect_columns);

		/* finally write the merged data back into the fragment arrays */
		tempvar = used_fragments; // necessary to pass value to getUsedFragments
		writeFromFragmentArrays( page_number );
	}
	//////////////////////////////////////////////////////////////////////
	/**
	 * get list of unused fragments and put in list
	 */
	// This method has been adapted from PdfGenericGrouping
	// in JPedal by IDR Solutions.  It is copyrighted by
	// IDR Solutions and licensed under the LGPL.
	// The value tempvar is passed so that it can enumerate
	// the used text fragments

	protected int[] getUnusedFragments()
	{
		//the following line has been added by Tamir Hassan
		int used_fragments = tempvar;
		//get unused item pointers
		int ii = 0;
		int temp_index[] = new int[used_fragments];
		// System.out.println("Total fragments: "+used_fragments);
		for( int i = 0;i < used_fragments;i++ )
		{
			int subscript = (int) order[i].intValue();
			if( isUsed[subscript] == false )
			{
				// System.out.println(subscript + " is used!");
				temp_index[ii] = subscript;
				ii++;
			}
		}
		int[] items = new int[ii];
		for( int pointer = 0;pointer < ii;pointer++ )
			items[pointer] = temp_index[pointer];
		return items;
	}

}
