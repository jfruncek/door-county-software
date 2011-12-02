

/* Filters rows in a table that don't partially match regex.
 * Also updates the session cookie with the filter value, and sets the info on what was filtered.
 */
function filterTable(regex, tableId, cellIndex) {
	document.cookie = tableId + "Filter=" + regex; // update the filter cookie
	var re = new RegExp(regex, "i");
	var table = document.getElementById(tableId);
	var element;
	var filterCount = 0;
	var dataLength = table.rows.length - 1; // skip header row
	var dataStartIndex = 1; 
	for ( var rowIndex = dataStartIndex; rowIndex < (dataStartIndex + dataLength); rowIndex++) { 
		element = table.rows[rowIndex].cells[cellIndex].innerHTML.replace(/<[^>]+>/g, "");
		var match = element.match(re);
		if (match == null) {
			table.rows[rowIndex].style.display = 'none';
			filterCount++;
		} else {
			table.rows[rowIndex].style.display = '';
		}
	}
	var sizeTd = document.getElementById(tableId + "FilterInfo");
	//alert("sizeTd is: " + sizeTd );
	sizeTd.innerHTML = "Showing " + (dataLength  - filterCount)  + " of " + dataLength + " rows";
}




/* Sets the filter from the session cookie, if available */ 
function setFilterFromCookie(tableId, columnIndex) {
	var filterValue = getSessionCookie(tableId + "Filter")
	document.filterForm.filter.value = filterValue;
	filterTable(filterValue, tableId, columnIndex);
}

/* Filters out rows in a table that don't partially match regex and are not already checked.
 * Assumption is that the checkbox is in the next cell after the filter data.
 * Also updates the session cookie with the filter value, and sets the info on what was filtered.
 */
function filterCheckableItemTable(regex, tableId, cellIndex) {
	document.cookie = tableId + "Filter=" + regex; // update the filter cookie
	var re = new RegExp(regex, "i");
	var table = document.getElementById(tableId);
	var element;
	var filterCount = 0;
	var dataLength = table.rows.length; // no header to skip
	var dataStartIndex = 0; 
	debugger;
	for ( var rowIndex = dataStartIndex; rowIndex < (dataStartIndex + dataLength); rowIndex++) { 
		element = table.rows[rowIndex].cells[cellIndex].innerHTML.replace(/<[^>]+>/g, "");
		var match = element.match(re);
		var checked = false;
		var length = table.rows[rowIndex].cells[cellIndex + 1].childNodes.length;
		for (var nodeIndex = 0; nodeIndex < length; nodeIndex++) {
			var obj = table.rows[rowIndex].cells[cellIndex + 1].childNodes[nodeIndex];
			if (obj.type == "checkbox" ) {
				if (obj.checked ) {
					checked = true;
				}
				//alert("rowIndex: " + rowIndex + ", nodeIndex: " +  nodeIndex +  "type: " + obj.type + ", value: " + obj.value + ", checked: " + obj.checked);
			}
		}
		if (match == null && !checked) {
			table.rows[rowIndex].style.display = 'none';
			filterCount++;
		} else {
			table.rows[rowIndex].style.display = '';
		}
	}
	var sizeTd = document.getElementById(tableId + "FilterInfo");
	sizeTd.innerHTML = "Showing " + (dataLength  - filterCount)  + " of " + dataLength + " rows. (Those matching filter, plus all checked)";
}



/* Sets the filter from the session cookie, if available */ 
function setCheckableItemFilterFromCookie(tableId, columnIndex) {
	var filterValue = getSessionCookie(tableId + "Filter")
	document.filterForm.filter.value = filterValue;
	filterCheckableItemTable(filterValue, tableId, columnIndex);
}



/* pulls a value in the form key=value; from the session cookie. */
function getSessionCookie(name) {
	var search = name + "="
	var returnvalue = "";
	if (document.cookie.length > 0) {
		offset = document.cookie.indexOf(search)
		// if cookie exists
		if (offset != -1) {
			offset += search.length
			// set index of beginning of value
			end = document.cookie.indexOf(";", offset);
			// set index of end of cookie value
			if (end == -1)
				end = document.cookie.length;
			returnvalue = unescape(document.cookie.substring(offset, end))
		}
	}
	return returnvalue;
}

/** Adds a function to be run when the page is finished loading. */
function addLoadEvent(func) {
	var oldonload = window.onload;
	if (typeof window.onload != 'function') {
		window.onload = func;
	} else {
		window.onload = function() {
			if (oldonload) {
				oldonload();
			}
			func();
		}
	}
}
