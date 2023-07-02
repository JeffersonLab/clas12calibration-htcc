import os
import argparse
import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd
import matplotlib.patches as mpatches
import matplotlib.lines as mlines

# Function to remove leading zeros from a string
def deleteLeadingZeros(inputString):
    for k in range(len(inputString)):
        if inputString[k] != '0':
            return int(inputString[k:])
    return 0

# Function to get existing paths for each run number and date
def get_existing_paths(top_directory, run_numbers, dates):
    run_info = {}
    for run_num in run_numbers:
        run_num_no_zeros = deleteLeadingZeros(run_num)
        run_info[run_num] = {
            "no_zeros": run_num_no_zeros,
            "dates": [],
            "paths": [],
            "correctionFactor_NphePMT": [],
            "correctionFactor_TimePMT": [],
        }
        for date in dates:
            possible_path = os.path.join(top_directory, run_num, date)
            if os.path.exists(possible_path):
                run_info[run_num]["dates"].append(date)
                run_info[run_num]["paths"].append(possible_path)
                nphe_file = os.path.join(possible_path, f"correctionFactor_NphePMT{run_num_no_zeros}.dat")
                time_file = os.path.join(possible_path, f"correctionFactor_TimePMT{run_num_no_zeros}.dat")
                if os.path.exists(nphe_file):
                    run_info[run_num]["correctionFactor_NphePMT"].append(nphe_file)
                if os.path.exists(time_file):
                    run_info[run_num]["correctionFactor_TimePMT"].append(time_file)
    return run_info

# Function to plot correction factors and percent changes
def plot_correction_factors_and_percent_changes(run_info):
    markers = ['o', 'v', '^', '<', '>', 's', 'p', '*', 'X', 'D', 'd']
    fig, axs = plt.subplots(2, 2, figsize=(15, 10))
    titles = ['nphe corrector factor per run', 'percent change from old ccdb gain constant to new', 'time shift to be applied to old ccdb time constants', 'percent change from old ccdb time constant to new']
    ylabels = ['correction factor = \nmean nphe per channel \nover average nphe over all channels', 'percent change [%]', 'time shift [ns]', 'percent change [%]']
    sector_patches = []
    for run_num, info in run_info.items():
        nphe_df = pd.read_csv(info["correctionFactor_NphePMT"][0]) if info["correctionFactor_NphePMT"] else None
        time_df = pd.read_csv(info["correctionFactor_TimePMT"][0]) if info["correctionFactor_TimePMT"] else None
        if nphe_df is not None:
            sectors = nphe_df['Sector'].unique()
            colors = sns.color_palette(n_colors=len(sectors))
            color_dict = dict(zip(sectors, colors))
            marker_dict = dict(zip(sectors, markers))
            sector_patches += [mlines.Line2D([], [], color=color_dict[sector], marker=marker_dict[sector], linestyle='None', markersize=10, label=f'Sector {sector}') for sector in sectors]
            for sector in sectors:
                df_sector = nphe_df[nphe_df['Sector'] == sector]
                axs[0, 0].scatter(int(run_num), df_sector['Factor'].mean(), color=color_dict[sector], marker=marker_dict[sector])
                axs[0, 1].scatter(int(run_num), df_sector['PercentChange'].mean(), color=color_dict[sector], marker=marker_dict[sector])
        if time_df is not None:
            sectors = time_df['Sector'].unique()
            colors = sns.color_palette(n_colors=len(sectors))
            color_dict = dict(zip(sectors, colors))
            marker_dict = dict(zip(sectors, markers))
            for sector in sectors:
                df_sector = time_df[time_df['Sector'] == sector]
                axs[1, 0].scatter(int(run_num), df_sector['TimeShift'].mean(), color=color_dict[sector], marker=marker_dict[sector])
                axs[1, 1].scatter(int(run_num), df_sector['PercentChange'].mean(), color=color_dict[sector], marker=marker_dict[sector])
    for i, ax in enumerate(axs.flatten()):
        ax.set_title(titles[i])
        ax.set_xlabel('Run Number')
        ax.set_ylabel(ylabels[i])
        if 'percent change [%]' in ylabels[i]:
            ax.axhline(5, color='gold', linestyle='--')
            ax.axhline(10, color='red', linestyle='--')
            ax.axhline(0, color='green', linestyle='-')
            ax.axhline(-5, color='gold', linestyle='--')
            ax.axhline(-10, color='red', linestyle='--')
    fig.suptitle('nphe and time Correction Factors and Percent Changes by Run Number and Sector', y=1.01)
    fig.legend(handles=sector_patches, bbox_to_anchor=(0.5, 1.01), loc='lower center', ncol=len(sectors))
    plt.tight_layout()
    plt.savefig('correction_factors_and_percent_changes_by_run_number_and_sector.png')
    plt.show()

if __name__ == "__main__":
    # Argument parsing
    parser = argparse.ArgumentParser(description='Process some integers.')
    parser.add_argument('--run_nums', nargs='+', required=True,
                        help='List of run numbers')
    parser.add_argument('--dates', nargs='+', required=True,
                        help='List of dates')
    parser.add_argument('--top_dir', required=True,
                        help='Top level directory where the data files are stored')
    args = parser.parse_args()
    run_info = get_existing_paths(args.top_dir, args.run_nums, args.dates)
    for run_num, info in run_info.items():
        print(f'Run number (with zeros): {run_num}')
        print(f'Run number (without zeros): {info["no_zeros"]}')
        print(f'Dates with valid info: {info["dates"]}')
        print(f'Paths: {info["paths"]}')
        print(f'correctionFactor_NphePMT: {info["correctionFactor_NphePMT"]}')
        print(f'correctionFactor_TimePMT: {info["correctionFactor_TimePMT"]}')
        print()
    plot_correction_factors_and_percent_changes(run_info)
    print("Plots saved to 'correction_factors_and_percent_changes_by_run_number_and_sector.png'")
