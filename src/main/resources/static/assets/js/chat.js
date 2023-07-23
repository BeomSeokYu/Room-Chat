let _user = "Player" + Math.floor(Math.random() * 1000000);

console.log = function(_msg) {
    $('#console .console-log').append('<li class="console-log-line">' + _msg.toString() + '</li>');
};

$( function() {
    $('.chat-input').submit( function(_event) {
        _event.preventDefault();
        let _date = new Date(),
            _time = [ _date.getHours(), _date.getMinutes(), _date.getSeconds() ].map( function(_n) {
                _n = _n.toString(10);
                if (_n.length === 1) { _n = "0" + _n; }
                return _n;
            } ).join(':'),
        _message = _time + ' - ' + _user + ' - ' + $('.chat-input-text').val();
        console.log(_time + ' - ' + _user + ' - ' + _message);
        $(this)[0].reset();
        sendDiceResults('chat', _message);
    } );
} );