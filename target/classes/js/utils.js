var join = function() {
    var name = '';
    for(var i = 0; i < arguments.length; i++) {
        if (arguments[i] !== null) {
            var next = arguments[i].replace(/^\.+/, '').replace(/\.+$/, '');
            if (next !== '') {
                if (name !== '') {
                    name += '.';
                }
                name += next;
            }
        }
    }
    return name;
};

var backward = function(packageName, lvl) {
    lvl = (typeof lvl !== 'undefined') ? lvl : 1;
    var finalName = packageName;
    for (var i = 0; i < lvl; i++) {
        if (packageName.lastIndexOf('.') >= 0) {
            finalName = finalName.substring(0, finalName.lastIndexOf('.'));
        }
    }
    return finalName;
};

var forward = function(packageName, lvl) {
    lvl = (typeof lvl !== 'undefined') ? lvl : 1;
    return backward(packageName, packageName.split('.').length - lvl);
};