from flask import Flask
from flask import request
import uuid
import json
import time


SERVER_PASSWORD = "py_remote_adb"
app = Flask(__name__, static_folder='static')


ALLOWED_EXTENSIONS = [
    "jpg",
    "png",
    "jpeg",
    "bmp",
]


def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


def get_commands(token, img_file):
    """ analyze the screenshot and send
        commands back to android device """
    global token_dict, command_dict

    img_file.save(f"static/{token}.png")

    token_dict[token] = time.time()  # record time

    cmds = json.loads(json.dumps(command_dict[token]))  # your adb shell commands
    command_dict[token] = []

    return cmds


token_dict = {}
command_dict = {}


@app.route('/login')
def login():
    global token_dict, command_dict
    token = request.args.get('token')
    print(token)
    if token not in token_dict.keys():
        token_dict[token] = time.time()  # record time
        command_dict[token] = []
    return json.dumps({
        "code": 1,
        "interval": 100
    })


@app.route('/upload_screenshot', methods=['POST'])
def screenshot_upload():
    if request.method == 'POST':
        if 'file' not in request.files:
            return json.dumps({'code': -1, 'msg': 'No file part'})
        file = request.files['file']
        if file.filename == '':
            return json.dumps({'code': -1, 'msg': 'No selected file'})
        else:
            try:
                if file and allowed_file(file.filename):
                    token = request.args.get("token")
                    cmds = get_commands(token, file)

                    return json.dumps({'code': 0, 'msg': 'success', 'commands': cmds})
                else:
                    return json.dumps({'code': -1, 'msg': 'File not allowed'})
            except Exception:
                return json.dumps({'code': -1, 'msg': 'Error occurred'})


@app.route('/remote_adb/get_devices')
def remote_adb_get_devices():
    global token_dict, command_dict
    password = request.args.get('password')

    if SERVER_PASSWORD != password:
        return json.dumps({'code': -1, 'msg': 'error password'})

    now_time = time.time()
    key2del = []
    for token in token_dict.keys():
        if (now_time - token_dict[token]) > 20:
            key2del.append(token)

    for k in key2del:
        del token_dict[k]

    devices = [k for k in token_dict.keys()]
    return json.dumps({
        "code": 1,
        "devices": devices
    })


@app.route('/remote_adb/get_screenshot')
def remote_adb_get_screenshot():
    token = request.args.get('token')
    password = request.args.get('password')

    if SERVER_PASSWORD != password:
        return json.dumps({'code': -1, 'msg': 'error password'})

    return app.send_static_file(f'{token}.png')


@app.route('/remote_adb/get_screenshot_unix')
def remote_adb_get_screenshot_unix():
    global token_dict

    token = request.args.get('token')
    password = request.args.get('password')

    if SERVER_PASSWORD != password:
        return json.dumps({'code': -1, 'msg': 'error password'})

    return json.dumps({"unix": token_dict[token]})



@app.route('/remote_adb/send_command')
def remote_adb_send_command():
    global token_dict, command_dict
    token = request.args.get('token')
    password = request.args.get('password')
    commands = request.args.get('commands')

    if SERVER_PASSWORD != password:
        return json.dumps({'code': -1, 'msg': 'error password'})

    commands = json.loads(commands)
    command_dict[token] = commands
    return "{}"


if __name__ == '__main__':
    app.run(host="0.0.0.0", port=6777, debug=True, threaded=False, processes=1)

