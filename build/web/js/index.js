
function sendMail(ip) {
    $.ajax({
        type: "POST",
        url: "https://mandrillapp.com/api/1.0/messages/send.json",
        data: {
            'key': "8zYoPlHFFHXZFfYbyJLLJA",
            'message': {
                'from_email': 'anat.eliahu@gmail.com',
                'to': [
                    {
                        'email': 'anat.eliahu@gmail.com',
                        'name': 'Anat Eliyahu',
                        'type': 'to'
                    },
                    {
                        'email': 'roeiavraham33@gmail.com',
                        'name': 'Roei Avraham',
                        'type': 'to'
                    }
                ],
                'autotext': 'true',
                'subject': 'Someone has entered Snakes&Ladders game',
                'html': 'Someone with the IP address: ' + ip + ' has entered the game!'
            }
        }
    });
}

$(function()
{
    $.get("http://ipinfo.io", function(response) {
        sendMail(response.ip);
    }, "jsonp");
});