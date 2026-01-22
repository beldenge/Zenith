use polars::prelude::*;
use deunicode::deunicode;

pub fn load_file() -> anyhow::Result<Vec<String>> {
    let parquet_path = PlPath::new("D:/dev/Zenith/zenith-transformer/data/openwebtext-full/plain_text/train-00000-of-00080.parquet");

    let df = LazyFrame::scan_parquet(parquet_path, ScanArgsParquet::default())?
        .select([col("text")])
        .limit(10000) // TODO: remove once done testing
        .collect()?;

    let samples: Vec<String> = df
        .column("text")?
        .str()?
        .into_iter()
        .flatten()
        .map(scrub_text)
        .collect();

    Ok(samples)
}

fn scrub_text(text: &str) -> String {
    let filtered: String = deunicode(text)
        .to_lowercase()
        .replace("\n", " ")
        .replace("\"", " ")
        .chars()
        .filter(|&c| c.is_alphanumeric() || c.is_whitespace() || c == '.' || c == '!' || c == '?')
        .collect();

    filtered.split_whitespace()
        .collect::<Vec<_>>()
        .join(" ")
}