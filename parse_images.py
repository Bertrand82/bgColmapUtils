import csv
import sqlite3
import numpy as np
from pyproj import Transformer
import os
from scipy.spatial.transform import Rotation as R
from pyproj import Geod
import json
import subprocess

import re

def parse_gps_string(gps_str):
    # Example: '52 deg 14\' 3.57" N'
    match = re.match(r"(\d+)\s*deg\s*(\d+)'[\s]*(\d+(?:\.\d+)?)\"?\s*([NSEW])", gps_str)
    if not match:
        raise ValueError(f"Invalid GPS format: {gps_str}")
    degrees = int(match.group(1))
    minutes = int(match.group(2))
    seconds = float(match.group(3))
    ref = match.group(4)
    return dms_to_decimal(degrees, minutes, seconds, ref)


def latlon_to_meters_delta(lat1, lon1, lat2, lon2):
    geod = Geod(ellps="WGS84")
    """
    Compute the local North/East (NED) offsets in meters from point1 → point2.
    Returns (north_m, east_m).
    """

    lat1,lon1,lat2,lon2 = np.broadcast_arrays(lat1, lon1, lat2, lon2)
    # Geodesic inverse: gives fwd_azimuth, back_azimuth, and distance

    az12, az21, dist = geod.inv(lon1, lat1, lon2, lat2)

    # Convert into north/east components
    # azimuth is degrees clockwise from north
    theta = np.radians(az12)

    north = dist * np.cos(theta)
    east  = dist * np.sin(theta)

    return east, north


# -------------------------
# CONFIG
# -------------------------
DB_FILE = "db.db"

db = sqlite3.connect(DB_FILE)
cur = db.cursor()

print("\n" + "=" * 80)
print("EXISTING ROWS IN DATABASE:")
print("=" * 80)
cur.execute("SELECT * FROM images")
rows = cur.fetchall()
if rows:
    # Get column names
    cur.execute("PRAGMA table_info(images)")
    columns = [col[1] for col in cur.fetchall()]
    print(f"\nColumns: {', '.join(columns)}")
    print(f"Total rows: {len(rows)}\n")
    for i, row in enumerate(rows, 1):
        print(f"Row {i}:")
        for col_name, value in zip(columns, row):
            print(f"  {col_name}: {value}")
        print()
else:
    print("No rows found in the database.\n")
print("=" * 80 + "\n")

# Camera mounting:
# DJI cameras usually look down (nadir)
# Body frame: X forward, Y right, Z down
# Camera frame: X right, Y down, Z forward
BODY_TO_CAM = R.from_euler("XYZ", [90, 0, 90], degrees=True)

def dms_to_decimal(degrees, minutes, seconds, ref):
    decimal = abs(degrees) + minutes / 60 + seconds / 3600
    if ref in ['S', 'W']:
        decimal = -decimal
    return decimal

# -------------------------
# Convert lat/lon/alt → ENU
# -------------------------
# WGS84 → ECEF
ecef = Transformer.from_crs("EPSG:4979", "EPSG:4978", always_xy=True)

xs, ys, zs = [], [], []
rs = []
first_lat,first_lon,first_alt = None,None,None
import exif
for file in os.listdir("images"):
    result = subprocess.run(
        ["exiftool", "-json", f"/images/{file}"],
        capture_output=True,
        text=True
    )

    metadata = json.loads(result.stdout)[0]
    with open(f"images/{file}", "rb") as f:
        bts = f.read()
        img = exif.Image(bts)

        lat = parse_gps_string(metadata["GPSLatitude"])
        lon = parse_gps_string(metadata["GPSLongitude"])
        alt = float(metadata["AbsoluteAltitude"])
        if first_lat is None:
            first_lat = lat
            first_lon = lon
            first_alt = alt
        rs.append({"pitch":float(metadata["GimbalPitchDegree"]), "roll":float(metadata["GimbalRollDegree"]), "yaw":float(metadata["GimbalYawDegree"]), "image_name":f"{file}"})
        zs.append(alt - first_alt)
        east,north = latlon_to_meters_delta(lat, lon, first_lat, first_lon)
        xs.append(east)
        ys.append(north)
    
# -------------------------
# Open COLMAP DB
# -------------------------


# -------------------------
# Inject pose priors
# -------------------------
for x,y,z,r in zip(xs,ys,zs,rs):
    name = r["image_name"]

    yaw = float(r["yaw"])
    pitch = float(r["pitch"])
    roll = float(r["roll"])
    print("yaw, pitch, roll" , yaw, pitch, roll)
    # Drone yaw/pitch/roll → body-to-world
    R_bw = R.from_euler(
        "ZYX",
        [yaw, pitch, roll],
        degrees=True
    )
    print("R_bw: \n", R_bw.as_matrix())

    # Camera-to-world
    R_cw = R_bw * BODY_TO_CAM
    print("R_cw: \n", R_cw.as_matrix())

    qx, qy, qz, qw = R_cw.as_quat()  # scipy order
    print("qw, qx, qy, qz: ", qw, qx, qy, qz)
    #cur.execute("""
    #    UPDATE images
    #    SET prior_tx=?, prior_ty=?, prior_tz=?,
    #        prior_qw=?, prior_qx=?, prior_qy=?, prior_qz=?
    #    WHERE name=?
    #""", (
    #    float(x), float(y), float(z),
    #    float(qw), float(qx), float(qy), float(qz),
    #    name
    #))

#db.commit()
db.close()
f.close()
print("Pose priors successfully injected.")
