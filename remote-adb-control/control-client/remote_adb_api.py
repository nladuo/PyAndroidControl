import requests
import json
import time


SERVER_PASSWORD = "py_remote_adb"
SERVER_URL = "http://127.0.0.1:6777"
last_screenshot_time = 0


def get_devices():
	url = f"{SERVER_URL}/remote_adb/get_devices?password={SERVER_PASSWORD}"
	resp = requests.get(url)
	content = resp.content.decode("utf-8")
	data = json.loads(content)
	return data["devices"]


def get_screenshot(device_token):
	global last_screenshot_time
	resp = requests.get(f"{SERVER_URL}/remote_adb/get_screenshot_unix?password={SERVER_PASSWORD}&token={device_token}")
	content = resp.content.decode("utf-8")
	data = json.loads(content)
	unix = data["unix"]

	while unix == last_screenshot_time:
		resp = requests.get(f"{SERVER_URL}/remote_adb/get_screenshot_unix?password={SERVER_PASSWORD}&token={device_token}")
		content = resp.content.decode("utf-8")
		data = json.loads(content)
		unix = data["unix"]
		time.sleep(0.5)
	# print(unix)
	last_screenshot_time = unix
	url = f"{SERVER_URL}/remote_adb/get_screenshot?password={SERVER_PASSWORD}&token={device_token}"
	resp = requests.get(url)
	with open(f"{device_token}.png", 'wb') as f:
		f.write(resp.content)


def send_commands(device_token, commands):
	commands = json.dumps(commands)
	url = f"{SERVER_URL}/remote_adb/send_command?password={SERVER_PASSWORD}&commands={commands}&token={device_token}"
	requests.get(url)


