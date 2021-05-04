from flask import Flask
from flask import Flask
from flask import request
import uuid
import json
from logics import get_commands

app = Flask(__name__)


ALLOWED_EXTENSIONS = [
    "jpg",
    "png",
    "jpeg",
    "bmp",
]


def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


@app.route('/login')
def login():
    token = request.args.get('token')
    return json.dumps({
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


if __name__ == '__main__':
    app.run(host="0.0.0.0", port=6777, debug=True)

