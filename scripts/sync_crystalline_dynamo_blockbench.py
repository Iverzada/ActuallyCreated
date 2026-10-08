"""Create editable Blockbench projects and export their cubes to Minecraft models.

Run --bootstrap only when intentionally replacing the .bbmodel sources from the
current Minecraft JSON files. Normal editing uses --export or --check.
"""

import argparse
import base64
import json
import uuid
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
MODELS = ROOT / "src/main/resources/assets/actuallycreated/models/block/crystalline_dynamo"
TEXTURES = ROOT / "src/main/resources/assets/actuallycreated/textures"
PROJECTS = ROOT / "blockbench/crystalline_dynamo"
NAMES = (
    "crystalline_dynamo",
    "left_jaw",
    "right_jaw",
    "shaft",
    "fire",
    "superheated_fire",
)
FACES = ("north", "east", "south", "west", "up", "down")
CREATE_JAR_DIR = Path.home() / ".gradle/caches/modules-2/files-2.1/com.simibubi.create/create-1.21.1"


def stable_id(*parts):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, "actuallycreated/blockbench/" + "/".join(parts)))


def implicit_uv(direction, start, end):
    x, y, z = start
    x2, y2, z2 = end
    return {
        "down": [x, 16 - z2, x2, 16 - z],
        "up": [x, z, x2, z2],
        "north": [16 - x2, 16 - y2, 16 - x, 16 - y],
        "south": [x, 16 - y2, x2, 16 - y],
        "west": [z, 16 - y2, z2, 16 - y],
        "east": [16 - z2, 16 - y2, 16 - z, 16 - y],
    }[direction]


def model_group(name, part):
    if name != "crystalline_dynamo":
        return "Peca movel" if name in ("left_jaw", "right_jaw", "shaft") else "Chama"
    part = part.lower()
    if "furnace" in part or "grille" in part:
        return "Fornalha"
    if "shaft" in part or "rear" in part:
        return "Encaixe traseiro"
    if "hopper" in part:
        return "Moagem superior"
    return "Carcaca"


def texture_bytes(resource, create_archive):
    namespace, path = resource.split(":", 1)
    if namespace == "actuallycreated":
        return (TEXTURES / f"{path}.png").read_bytes()
    if namespace == "create":
        return create_archive.read(f"assets/create/textures/{path}.png")
    raise ValueError(f"Unsupported texture namespace: {resource}")


def bootstrap():
    jars = sorted(CREATE_JAR_DIR.rglob("*slim.jar"))
    if not jars:
        raise FileNotFoundError("Create slim jar missing from the Gradle cache")
    PROJECTS.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(jars[-1]) as create_archive:
        for name in NAMES:
            minecraft = json.loads((MODELS / f"{name}.json").read_text(encoding="utf-8"))
            keys = list(dict.fromkeys(
                face["texture"][1:]
                for element in minecraft["elements"]
                for face in element["faces"].values()
            ))
            textures = []
            for index, key in enumerate(keys):
                resource = minecraft["textures"][key]
                data = texture_bytes(resource, create_archive)
                textures.append({
                    "name": resource.rsplit("/", 1)[-1] + ".png",
                    "id": str(index),
                    "uuid": stable_id(name, "texture", key),
                    "source": "data:image/png;base64," + base64.b64encode(data).decode("ascii"),
                    "internal": True,
                    "visible": True,
                })

            elements = []
            grouped = {}
            for index, part in enumerate(minecraft["elements"]):
                label = part.get("name", f"cube_{index + 1}")
                cube_id = stable_id(name, "cube", str(index), label)
                faces = {}
                for direction in FACES:
                    source_face = part["faces"].get(direction)
                    if source_face is None:
                        faces[direction] = {"texture": None, "enabled": False}
                        continue
                    face = {
                        "uv": source_face.get("uv", implicit_uv(direction, part["from"], part["to"])),
                        "texture": keys.index(source_face["texture"][1:]),
                    }
                    if "rotation" in source_face:
                        face["rotation"] = source_face["rotation"]
                    faces[direction] = face
                elements.append({
                    "name": label,
                    "type": "cube",
                    "uuid": cube_id,
                    "from": part["from"],
                    "to": part["to"],
                    "origin": [8, 8, 8],
                    "autouv": 0,
                    "faces": faces,
                })
                grouped.setdefault(model_group(name, label), []).append(cube_id)

            groups = []
            outliner = []
            for index, (label, children) in enumerate(grouped.items()):
                group_id = stable_id(name, "group", label)
                groups.append({"name": label, "uuid": group_id, "origin": [8, 8, 8], "color": index % 8})
                outliner.append({"uuid": group_id, "isOpen": True, "children": children})
            project = {
                "meta": {"format_version": "5.0", "model_format": "java_block", "box_uv": False},
                "name": name,
                "resolution": {"width": 16, "height": 16},
                "elements": elements,
                "groups": groups,
                "outliner": outliner,
                "textures": textures,
            }
            (PROJECTS / f"{name}.bbmodel").write_text(
                json.dumps(project, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
            )
            print(f"Created {name}.bbmodel ({len(elements)} cubes)")


def export(check=False):
    changed = []
    for name in NAMES:
        project = json.loads((PROJECTS / f"{name}.bbmodel").read_text(encoding="utf-8"))
        if project.get("meta", {}).get("model_format") != "java_block":
            raise ValueError(f"{name}: expected a Java Block/Item project")
        target = MODELS / f"{name}.json"
        minecraft = json.loads(target.read_text(encoding="utf-8"))
        original = json.loads(target.read_text(encoding="utf-8"))
        old_keys = {path: key for key, path in minecraft["textures"].items() if key != "particle"}
        resources = {}
        for index, texture in enumerate(project["textures"]):
            stem = Path(texture["name"]).stem
            matches = [path for path in old_keys if path.endswith("/" + stem)]
            if len(matches) != 1:
                raise ValueError(f"{name}: cannot identify texture {texture['name']}")
            resources[index] = matches[0]
            resources[texture.get("uuid")] = matches[0]
            resources[texture.get("id")] = matches[0]

        elements = []
        used_keys = set()
        for cube in project["elements"]:
            if cube.get("type") != "cube" or cube.get("export") is False:
                continue
            part = {"name": cube.get("name", "cube"), "from": cube["from"], "to": cube["to"], "faces": {}}
            for direction in FACES:
                source = cube.get("faces", {}).get(direction)
                if not source or source.get("enabled") is False or source.get("texture") is None:
                    continue
                resource = resources.get(source["texture"])
                if resource is None:
                    raise ValueError(f"{name}/{part['name']}/{direction}: unknown texture")
                key = old_keys[resource]
                used_keys.add(key)
                face = {"texture": "#" + key}
                uv = source.get("uv")
                if uv is not None and uv != implicit_uv(direction, cube["from"], cube["to"]):
                    face["uv"] = uv
                if source.get("rotation"):
                    face["rotation"] = source["rotation"]
                part["faces"][direction] = face
            if part["faces"]:
                elements.append(part)
        if not elements:
            raise ValueError(f"{name}: project has no exported cubes")
        minecraft["elements"] = elements
        minecraft["textures"] = {key: value for key, value in minecraft["textures"].items()
                                 if key in used_keys or key == "particle"}
        result = json.dumps(minecraft, indent=2, ensure_ascii=False) + "\n"

        def shape(model):
            return [
                (
                    part.get("name", f"cube_{index + 1}"),
                    tuple(part["from"]),
                    tuple(part["to"]),
                    tuple(sorted(
                        (direction,
                         model["textures"][face["texture"][1:]],
                         tuple(face.get("uv", implicit_uv(direction, part["from"], part["to"]))),
                         face.get("rotation", 0))
                        for direction, face in part["faces"].items()
                    )),
                )
                for index, part in enumerate(model["elements"])
            ]

        if shape(minecraft) != shape(original):
            changed.append(name)
            if not check:
                target.write_text(result, encoding="utf-8")
    if check:
        if changed:
            raise SystemExit("Models differ from Blockbench projects: " + ", ".join(changed))
        print("All Minecraft JSON models match their Blockbench projects")
    else:
        print("Exported: " + (", ".join(changed) if changed else "no changes"))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    choice = parser.add_mutually_exclusive_group(required=True)
    choice.add_argument("--bootstrap", action="store_true", help="replace .bbmodel sources from Minecraft JSON")
    choice.add_argument("--export", action="store_true", help="write Minecraft JSON from edited .bbmodel projects")
    choice.add_argument("--check", action="store_true", help="check that Minecraft JSON matches .bbmodel projects")
    args = parser.parse_args()
    if args.bootstrap:
        bootstrap()
    else:
        export(check=args.check)
