/*
 * Copyright (C) 2012 thomas
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nfp.view

import java.util.{Enumeration, ResourceBundle}
import java.lang.String


/**
 * Provides internationalization.
 *
 * @author Thomas Geier
 * Date: 24.01.12
 */

object I18n {
  val bundle = ResourceBundle.getBundle("nfp.translation")

  implicit def symbol2LocalizedString(s: Symbol): String = new String(bundle.getString(s.name).getBytes("ISO-8859-1"), "UTF-8")
}
