<?php
    if (isset($_COOKIE['count'])) {
        $count = $_COOKIE['count'] + 1;
    } else {
        $count = 1;
    }
    setcookie('count', $count, time()+3600);
    setcookie("Cart[$count]", $item, time()+3600);
?>

<form action="" method="post">
    Name:  <input type="text" name="personal[name]" /><br />
    Email: <input type="text" name="personal[email]" /><br />
    Beer: <br />
    <select multiple name="beer[]" size="3">
        <option value="warthog">Warthog</option>
        <option value="guinness">Guinness</option>
        <option value="stuttgarter">Stuttgarter Schwabenbräu</option>
        <option value="newglarus">New Glarus</option>
        <option value="point">Point</option>
        <option value="lakeside">Lakeside</option>
    </select><br>

    <input type="checkbox" id="cb1" name="items[]" value="Eastside Dark" />
    <label for="cb1" >Eastside Dark</label><br />
    <input type="checkbox" id="cb2" name="items[]" value="Cream City Ale" />
    <label for="cb1" >Cream City Ale</label><br />
    <input type="checkbox" id="cb3" name="items[]" value="Fuel Cafe Coffee Flavored Stout" />
    <label for="cb1" >Fuel Cafe Coffee Flavored Stout</label><br />
    <input type="checkbox" id="cb4" name="items[]" value="India Pale Ale" />
    <label for="cb1" >India Pale Ale</label><br />
    <input type="checkbox" id="cb5" name="items[]" value="Riverwest Stein Beer" />
    <label for="cb1" >Riverwest Stein Beer</label><br />
    
    <INPUT type="radio" name="kind" value="Lager"> Lager<BR>
    <INPUT type="radio" name="kind" value="Ale"> Ale<BR>
    <br />
    <input type="submit" value="Buy beer" />
    <input type="image" src="beer.jpg" name="sub" alt="Reset shopping cart"/>

</form>

<?php

if ($_GET) {
    echo '<h3>_GET</h3>';
    echo '<pre>';
    echo htmlspecialchars(print_r($_GET, true));
    echo '</pre>';
}

if ($_POST) {
    echo '<h3>_POST</h3>';
    echo '<pre>';
    echo htmlspecialchars(print_r($_POST, true));
    echo '</pre>';
}

if ($_COOKIE) {
    echo '<h3>$_COOKIE</h3>';
    echo '<pre>';
    echo htmlspecialchars(print_r($_COOKIE, true));
    echo '</pre>';
}

if ($_REQUEST) {
    echo '<h3>$_REQUEST</h3>';
    echo '<pre>';
    echo htmlspecialchars(print_r($_REQUEST, true));
    echo '</pre>';
}
?>
