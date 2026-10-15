import json

with open('panama.geo.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

coords = data['features'][0]['geometry']['coordinates'][0]
with open('coords.txt', 'w', encoding='utf-8') as out:
    for c in coords:
        out.write(f"Pair({c[1]:.4f}, {c[0]:.4f}),\n")

print("Done, total points:", len(coords))
