/* Refresh the saved GitHub release snapshot whenever this page is opened. */
(() => {
  const repositories = [
    { name: "catalog", repository: "geoportal-server-catalog" },
    { name: "harvester", repository: "geoportal-server-harvester" },
  ];

  const dateFormat = new Intl.DateTimeFormat("en-US", {
    timeZone: "UTC",
    year: "numeric",
    month: "long",
    day: "numeric",
  });

  function releaseLink(release, repository) {
    const url = new URL(release.html_url);
    const prefix = `/Esri/${repository}/releases/tag/`;
    if (url.protocol !== "https:" || url.hostname !== "github.com" ||
        !url.pathname.startsWith(prefix)) {
      throw new Error("Unexpected GitHub release link");
    }
    return url.href;
  }

  async function updateReleases({ name, repository }) {
    const table = document.getElementById(`${name}-releases`);
    const status = document.getElementById(`${name}-release-status`);
    if (!table || !status) return;

    try {
      const controller = new AbortController();
      const timeout = setTimeout(() => controller.abort(), 10000);
      let response;
      try {
        response = await fetch(
          `https://api.github.com/repos/Esri/${repository}/releases?per_page=100`,
          { headers: { Accept: "application/vnd.github+json" }, signal: controller.signal },
        );
      } finally {
        clearTimeout(timeout);
      }
      if (!response.ok) throw new Error(`GitHub returned ${response.status}`);
      const releases = await response.json();
      if (!Array.isArray(releases)) throw new Error("Invalid GitHub response");

      const rows = releases
        .filter((release) => !release.draft && release.published_at && release.tag_name)
        .sort((a, b) => Date.parse(b.published_at) - Date.parse(a.published_at))
        .map((release) => {
          const date = new Date(release.published_at);
          if (Number.isNaN(date.getTime())) throw new Error("Invalid release date");
          const row = document.createElement("tr");
          const version = document.createElement("th");
          version.scope = "row";
          version.textContent = release.tag_name;
          const published = document.createElement("td");
          published.textContent = dateFormat.format(date);
          const notes = document.createElement("td");
          const link = document.createElement("a");
          link.href = releaseLink(release, repository);
          link.textContent = "GitHub release notes";
          notes.append(link);
          if (release.prerelease) notes.append(" (pre-release)");
          row.append(version, published, notes);
          return row;
        });
      if (!rows.length) throw new Error("No published releases returned");

      table.tBodies[0].replaceChildren(table.tBodies[0].rows[0].cloneNode(true), ...rows);
      status.firstChild.textContent = `Updated from GitHub ${dateFormat.format(new Date())}. `;
    } catch (error) {
      status.firstChild.textContent = "Showing saved releases; live GitHub data is unavailable. ";
      console.warn(`Could not update ${repository} releases:`, error);
    }
  }

  repositories.forEach(updateReleases);
})();
